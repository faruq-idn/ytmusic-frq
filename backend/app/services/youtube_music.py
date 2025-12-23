"""
YouTube Music service using ytmusicapi.
Handles search, metadata, lyrics, and related songs.
"""
import re
from typing import List, Optional
from ytmusicapi import YTMusic

from app.models.song import Song
from app.models.playlist import Playlist
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
    
    def get_search_suggestions(self, query: str) -> List[str]:
        """
        Get search suggestions for a query.
        
        Args:
            query: Partial search query
        
        Returns:
            List of suggestion strings
        """
        if not query or len(query) < 2:
            return []
        
        try:
            results = self._client.get_search_suggestions(query)
            # Results is a list of suggestion strings
            return results[:10]  # Limit to 10 suggestions
        except Exception:
            return []
    
    def search_playlists(self, query: str, limit: int = 10) -> List[Playlist]:
        """
        Search for playlists on YouTube Music.
        
        Args:
            query: Search query string
            limit: Maximum number of results (default 10)
        
        Returns:
            List of Playlist objects
        """
        results = self._client.search(query, filter="playlists", limit=limit)
        
        playlists = []
        for item in results:
            # Get highest quality thumbnail
            thumbnail_url = ""
            if item.get("thumbnails"):
                thumbnail_url = transform_thumbnail_url(
                    item["thumbnails"][-1]["url"]
                )
            
            # Extract song count from itemCount or description
            # Extract song count from itemCount
            song_count = None
            item_count_text = item.get("itemCount")
            if item_count_text:
                try:
                    # Remove " songs", " song" (case insensitive), and commas
                    clean_text = str(item_count_text).lower().replace(" songs", "").replace(" song", "").replace(",", "").strip()
                    # Extract first number found if mixed with other text
                    import re
                    match = re.search(r'\d+', clean_text)
                    if match:
                        song_count = int(match.group())
                except:
                    pass
            
            playlists.append(Playlist(
                playlist_id=item.get("browseId", ""),
                title=item.get("title", "Unknown Playlist"),
                description=item.get("description"),
                thumbnail_url=thumbnail_url,
                song_count=song_count,
                author=item.get("author", None)
            ))
        
        return playlists
    
    def get_playlist(self, playlist_id: str) -> Optional[dict]:
        """
        Get playlist details including all songs.
        
        Args:
            playlist_id: YouTube Music playlist/browse ID
        
        Returns:
            Dict with playlist info and songs, or None if not found
        """
        try:
            data = self._client.get_playlist(playlist_id)
            
            # Get thumbnail
            thumbnail_url = ""
            if data.get("thumbnails"):
                thumbnail_url = transform_thumbnail_url(
                    data["thumbnails"][-1]["url"]
                )
            
            # Parse songs
            songs = []
            for item in data.get("tracks", []):
                song_thumbnail = ""
                if item.get("thumbnails"):
                    song_thumbnail = transform_thumbnail_url(
                        item["thumbnails"][-1]["url"]
                    )
                
                songs.append(Song(
                    video_id=item.get("videoId", ""),
                    title=item.get("title", "Unknown"),
                    artist=self._get_artist_name(item),
                    album=self._get_album_name(item),
                    duration_text=item.get("duration", ""),
                    thumbnail_url=song_thumbnail
                ))
            
            return {
                "playlist_id": playlist_id,
                "title": data.get("title", "Unknown Playlist"),
                "description": data.get("description"),
                "thumbnail_url": thumbnail_url,
                "author": data.get("author", {}).get("name") if data.get("author") else None,
                "song_count": len(songs),
                "songs": songs
            }
        except Exception as e:
            print(f"Error fetching playlist: {e}")
            return None
    
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
        Tries LRCLIB first for synced lyrics, then falls back to YouTube Music.
        
        Args:
            video_id: YouTube video ID
        
        Returns:
            LyricsData with structured lyrics or None
        """
        # First, get song info from YouTube Music to get title/artist
        try:
            watch = self._client.get_watch_playlist(video_id)
            if not watch or not watch.get("tracks"):
                return None
            
            current_track = watch["tracks"][0]
            title = current_track.get("title", "")
            artist = self._get_artist_name(current_track)
            duration_sec = self._parse_duration(current_track.get("length", "0:00"))
            
            # Try LRCLIB for synced lyrics
            synced_lyrics = self._get_lrclib_lyrics(title, artist, duration_sec)
            if synced_lyrics:
                return synced_lyrics
            
            # Fallback to YouTube Music lyrics (usually plain text)
            lyrics_id = watch.get("lyrics")
            if lyrics_id:
                lyrics_data = self._client.get_lyrics(lyrics_id)
                raw_lyrics = lyrics_data.get("lyrics")
                if raw_lyrics:
                    # Parse as plain lyrics
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
            
            return None
        except Exception as e:
            print(f"Lyrics error: {e}")
            return None
    
    def _get_lrclib_lyrics(self, title: str, artist: str, duration_sec: int) -> Optional[LyricsData]:
        """
        Fetch synced lyrics from LRCLIB API.
        
        Args:
            title: Song title
            artist: Artist name
            duration_sec: Song duration in seconds
        
        Returns:
            LyricsData with synced lyrics or None
        """
        import requests
        import urllib.parse
        
        try:
            # Try to get synced lyrics from LRCLIB
            url = f"https://lrclib.net/api/get?artist_name={urllib.parse.quote(artist)}&track_name={urllib.parse.quote(title)}&duration={duration_sec}"
            
            response = requests.get(url, timeout=5)
            if response.status_code != 200:
                return None
            
            data = response.json()
            synced_lyrics = data.get("syncedLyrics")
            
            if synced_lyrics:
                # Parse LRC format: [mm:ss.xx] lyrics text
                lrc_pattern = r'\[(\d{2}):(\d{2})\.(\d{2,3})\](.+)'
                lines_with_time = re.findall(lrc_pattern, synced_lyrics)
                
                if lines_with_time:
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
                        source="lrclib"
                    )
            
            # Try plain lyrics from LRCLIB if no synced
            plain_lyrics = data.get("plainLyrics")
            if plain_lyrics:
                plain_lines = [
                    line.strip() for line in plain_lyrics.split('\n') 
                    if line.strip()
                ]
                lyrics_lines = [
                    LyricsLine(text=line) for line in plain_lines
                ]
                
                return LyricsData(
                    type="plain",
                    lines=lyrics_lines,
                    source="lrclib"
                )
            
            return None
        except Exception as e:
            print(f"LRCLIB error: {e}")
            return None
    
    def _parse_duration(self, duration_str: str) -> int:
        """Parse duration string (e.g., '3:45') to seconds."""
        try:
            parts = duration_str.split(':')
            if len(parts) == 2:
                return int(parts[0]) * 60 + int(parts[1])
            elif len(parts) == 3:
                return int(parts[0]) * 3600 + int(parts[1]) * 60 + int(parts[2])
            return 0
        except:
            return 0
    
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
