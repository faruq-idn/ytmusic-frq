"""
Lyrics-related models for structured lyrics response.
Supports both plain text and synced (timed) lyrics.
"""
from typing import List, Optional
from pydantic import BaseModel


class LyricsLine(BaseModel):
    """A single line of lyrics with optional timing information."""
    text: str
    start_time_ms: Optional[int] = None
    end_time_ms: Optional[int] = None


class LyricsData(BaseModel):
    """
    Structured lyrics response.
    
    Attributes:
        type: "synced" if lyrics have timing, "plain" if not
        lines: List of lyrics lines
        source: Source of lyrics (e.g., "youtube_music")
    """
    type: str  # "synced" | "plain"
    lines: List[LyricsLine]
    source: str = "youtube_music"


class LyricsResponse(BaseModel):
    """Response model for lyrics endpoint."""
    video_id: str
    has_lyrics: bool
    lyrics: Optional[LyricsData] = None
