"""
YouTube Music service using ytmusicapi.
Handles search, metadata, lyrics, and related songs.
"""
import re
from typing import List, Optional
from ytmusicapi import YTMusic

from app.models.song import Song
from app.models.lyrics import LyricsData, LyricsLine
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
    
    def get_lyrics(self, video_id: str) -> Optional[LyricsData]:
        """
        Get lyrics for a song with timing information if available.
        
        Args:
            video_id: YouTube video ID
        
        Returns:
            LyricsData with structured lyrics or None
        """
        try:
            watch = self._client.get_watch_playlist(video_id)
            lyrics_id = watch.get("lyrics")
            
            if not lyrics_id:
                return None
            
            lyrics_data = self._client.get_lyrics(lyrics_id)
            raw_lyrics = lyrics_data.get("lyrics")
            
            if not raw_lyrics:
                return None
            
            # Check if lyrics have timing information (LRC format)
            # LRC format: [mm:ss.xx] lyrics text
            lrc_pattern = r'\[(\d{2}):(\d{2})\.(\d{2,3})\](.+)'
            lines_with_time = re.findall(lrc_pattern, raw_lyrics)
            
            if lines_with_time:
                # Synced lyrics detected
                lyrics_lines = []
                for i, (minutes, seconds, ms, text) in enumerate(lines_with_time):
                    start_ms = (int(minutes) * 60 + int(seconds)) * 1000 + int(ms.ljust(3, '0')[:3])
                    
                    # Calculate end time (start of next line or +5 seconds)
                    if i < len(lines_with_time) - 1:
                        next_min, next_sec, next_ms, _ = lines_with_time[i + 1]
                        end_ms = (int(next_min) * 60 + int(next_sec)) * 1000 + int(next_ms.ljust(3, '0')[:3])
                    else:
                        end_ms = start_ms + 5000
                    
                    lyrics_lines.append(LyricsLine(
                        text=text.strip(),
                        start_time_ms=start_ms,
                        end_time_ms=end_ms
                    ))
                
                return LyricsData(
                    type="synced",
                    lines=lyrics_lines,
                    source="youtube_music"
                )
            else:
                # Plain lyrics - split by newlines
                plain_lines = [
                    line.strip() for line in raw_lyrics.split('\n') 
                    if line.strip()
                ]
                lyrics_lines = [
                    LyricsLine(text=line) for line in plain_lines
                ]
                
                return LyricsData(
                    type="plain",
                    lines=lyrics_lines,
                    source="youtube_music"
                )
                
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


    def download_audio(self, video_id: str, quality: str = "best") -> Optional[str]:
        """
        Download audio for a song.
        
        Args:
            video_id: YouTube video ID
            quality: Audio quality (best, 128k, 256k)
            
        Returns:
            Path to downloaded file or None
        """
        import yt_dlp
        import tempfile
        import os
        
        try:
            # Create temp directory specific to our app
            temp_dir = os.path.join(tempfile.gettempdir(), "ytmusic_downloads")
            os.makedirs(temp_dir, exist_ok=True)
            
            output_template = os.path.join(temp_dir, f"{video_id}.%(ext)s")
            
            # Map quality to format
            # formats: bestaudio/best, or specific bitrate
            if quality == "best":
                format_spec = "bestaudio/best"
            else:
                # fallback to best if specific not found, but try to limit size?
                # for now just use bestaudio which is usually opus/m4a
                format_spec = "bestaudio/best"
            
            ydl_opts = {
                'format': format_spec,
                'outtmpl': output_template,
                'quiet': True,
                'no_warnings': True,
                'extract_flat': False,
                'postprocessors': [{
                    'key': 'FFmpegExtractAudio',
                    'preferredcodec': 'm4a', # m4a is widely supported on Android
                    'preferredquality': '192',
                }],
            }
            
            with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                info = ydl.extract_info(video_id, download=True)
                filename = ydl.prepare_filename(info)
                # FFmpeg converter changes extension to m4a
                final_filename = os.path.splitext(filename)[0] + ".m4a"
                
                if os.path.exists(final_filename):
                    return final_filename
                
            return None
        except Exception as e:
            print(f"Download error: {e}")
            return None

# Singleton service instance
yt_music_service = YouTubeMusicService()
