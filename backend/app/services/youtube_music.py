"""
YouTube Music service using ytmusicapi.
Handles search, metadata, and related songs.
"""
from typing import List, Optional
from ytmusicapi import YTMusic

from app.models.song import Song
from app.utils.thumbnail import transform_thumbnail_url


class YouTubeMusicService:
    """Service for interacting with YouTube Music API."""
    
    def __init__(self):
        """Initialize YTMusic client (unauthenticated)."""
        self._client = YTMusic()
    
    def search_songs(self, query: str, limit: int = 20) -> List[Song]:
        """
        Search for songs on YouTube Music.
        
        Args:
            query: Search query string
            limit: Maximum number of results (default 20)
        
        Returns:
            List of Song objects
        """
        results = self._client.search(query, filter="songs", limit=limit)
        
        songs = []
        for item in results:
            # Get highest quality thumbnail
            thumbnail_url = ""
            if item.get("thumbnails"):
                thumbnail_url = transform_thumbnail_url(
                    item["thumbnails"][-1]["url"]
                )
            
            songs.append(Song(
                video_id=item.get("videoId", ""),
                title=item.get("title", "Unknown"),
                artist=self._get_artist_name(item),
                album=self._get_album_name(item),
                duration_text=item.get("duration", ""),
                thumbnail_url=thumbnail_url
            ))
        
        return songs
    
    def get_song_metadata(self, video_id: str) -> Optional[dict]:
        """
        Get detailed metadata for a song.
        
        Args:
            video_id: YouTube video ID
        
        Returns:
            Song metadata dict or None if not found
        """
        try:
            data = self._client.get_song(video_id)
            return data
        except Exception:
            return None
    
    def get_watch_playlist(self, video_id: str) -> Optional[dict]:
        """
        Get watch playlist (for related songs and lyrics).
        
        Args:
            video_id: YouTube video ID
        
        Returns:
            Watch playlist data or None
        """
        try:
            return self._client.get_watch_playlist(video_id)
        except Exception:
            return None
    
    def get_related_songs(self, video_id: str, limit: int = 20) -> List[Song]:
        """
        Get related/recommended songs based on a video.
        
        Args:
            video_id: YouTube video ID
            limit: Maximum number of results
        
        Returns:
            List of related Song objects
        """
        try:
            playlist = self._client.get_watch_playlist(video_id)
            # Skip first track (it's the current song)
            tracks = playlist.get("tracks", [])[1:limit + 1]
            
            songs = []
            for item in tracks:
                # Handle different thumbnail formats
                thumbnail_url = ""
                thumbnail_data = item.get("thumbnail")
                if thumbnail_data:
                    if isinstance(thumbnail_data, list) and thumbnail_data:
                        thumbnail_url = transform_thumbnail_url(
                            thumbnail_data[-1].get("url", "")
                        )
                    elif isinstance(thumbnail_data, dict):
                        thumbnails = thumbnail_data.get("thumbnails", [])
                        if thumbnails:
                            thumbnail_url = transform_thumbnail_url(
                                thumbnails[-1].get("url", "")
                            )
                
                songs.append(Song(
                    video_id=item.get("videoId", ""),
                    title=item.get("title", "Unknown"),
                    artist=self._get_artist_name(item),
                    album=self._get_album_name(item),
                    duration_text=item.get("length", ""),
                    thumbnail_url=thumbnail_url
                ))
            
            return songs
        except Exception:
            return []
    
    def get_lyrics(self, video_id: str) -> Optional[str]:
        """
        Get lyrics for a song if available.
        
        Args:
            video_id: YouTube video ID
        
        Returns:
            Lyrics string or None
        """
        try:
            watch = self._client.get_watch_playlist(video_id)
            lyrics_id = watch.get("lyrics")
            
            if lyrics_id:
                lyrics_data = self._client.get_lyrics(lyrics_id)
                return lyrics_data.get("lyrics")
            
            return None
        except Exception:
            return None
    
    def _get_artist_name(self, item: dict) -> str:
        """Extract artist name from item."""
        artists = item.get("artists", [])
        if artists:
            return artists[0].get("name", "Unknown Artist")
        return "Unknown Artist"
    
    def _get_album_name(self, item: dict) -> Optional[str]:
        """Extract album name from item."""
        album = item.get("album")
        if album:
            return album.get("name")
        return None


# Singleton service instance
yt_music_service = YouTubeMusicService()
