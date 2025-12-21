"""
Stream extractor service using yt-dlp.
Extracts direct audio stream URLs from YouTube Music.
"""
import yt_dlp
from typing import Optional
from dataclasses import dataclass


@dataclass
class StreamInfo:
    """Information about an audio stream."""
    url: str
    format: str
    quality: Optional[str]
    expires_in_seconds: int = 21600  # ~6 hours (YouTube default)


class StreamExtractorService:
    """Service for extracting audio stream URLs using yt-dlp."""
    
    def __init__(self):
        """Initialize yt-dlp options for audio extraction."""
        self._ydl_opts = {
            'format': 'm4a/bestaudio/best',
            'quiet': True,
            'no_warnings': True,
            'extract_flat': False,
            'no_check_certificate': True,
        }
    
    def get_stream_url(self, video_id: str) -> StreamInfo:
        """
        Extract direct audio stream URL for a video.
        
        Args:
            video_id: YouTube video ID (11 characters)
        
        Returns:
            StreamInfo with URL, format, and quality
        
        Raises:
            Exception: If extraction fails
        """
        url = f"https://music.youtube.com/watch?v={video_id}"
        
        with yt_dlp.YoutubeDL(self._ydl_opts) as ydl:
            info = ydl.extract_info(url, download=False)
            
            # Get available formats
            formats = info.get('formats', [])
            audio_format = self._get_best_audio_format(formats)
            
            if not audio_format:
                raise Exception("No audio format found")
            
            # Get quality info
            quality = audio_format.get('abr')
            quality_str = f"{int(quality)}kbps" if quality else None
            
            return StreamInfo(
                url=audio_format['url'],
                format=audio_format.get('ext', 'm4a'),
                quality=quality_str,
            )
    
    def _get_best_audio_format(self, formats: list) -> Optional[dict]:
        """
        Get the best audio-only format, preferring m4a.
        
        Args:
            formats: List of available formats from yt-dlp
        
        Returns:
            Best audio format dict or None
        """
        # Filter to audio-only formats
        audio_formats = [
            f for f in formats 
            if f.get('acodec') != 'none' and f.get('vcodec') in ('none', None)
        ]
        
        if not audio_formats:
            # Fallback: any format with audio
            audio_formats = [f for f in formats if f.get('acodec') != 'none']
        
        if not audio_formats:
            return None
        
        # Prefer m4a format
        m4a_formats = [f for f in audio_formats if f.get('ext') == 'm4a']
        if m4a_formats:
            # Get highest bitrate m4a
            return max(m4a_formats, key=lambda f: f.get('abr', 0) or 0)
        
        # Fallback to highest bitrate of any format
        return max(audio_formats, key=lambda f: f.get('abr', 0) or 0)


# Singleton service instance
stream_extractor_service = StreamExtractorService()
