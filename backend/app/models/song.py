"""
Song-related models for YouTube Music data.
"""
from typing import Optional, List
from pydantic import BaseModel


class Song(BaseModel):
    """Represents a song from YouTube Music."""
    video_id: str
    title: str
    artist: str
    album: Optional[str] = None
    duration_text: Optional[str] = None
    thumbnail_url: str


class SearchMeta(BaseModel):
    """Metadata for search results."""
    query: str
    count: int


class SearchResponse(BaseModel):
    """Response model for search endpoint."""
    songs: List[Song]
    meta: SearchMeta


class StreamData(BaseModel):
    """Response model for stream endpoint."""
    video_id: str
    stream_url: str
    format: str
    quality: Optional[str] = None
    expires_in_seconds: int


class MetadataResponse(BaseModel):
    """Response model for metadata endpoint."""
    video_id: str
    title: str
    artist: str
    album: Optional[str] = None
    duration_seconds: int
    has_lyrics: bool
    lyrics: Optional[str] = None


class RelatedResponse(BaseModel):
    """Response model for related songs endpoint."""
    songs: List[Song]
