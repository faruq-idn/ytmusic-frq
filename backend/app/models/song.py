"""
Song-related models for YouTube Music data.
"""
from typing import Optional, List, Any
from pydantic import BaseModel

from app.models.playlist import Playlist
from app.models.album import Album
from app.models.artist import Artist


class Song(BaseModel):
    """Represents a song from YouTube Music."""
    video_id: str
    title: str
    artist: str
    artist_id: Optional[str] = None  # browseId for artist navigation
    album: Optional[str] = None
    duration_text: Optional[str] = None
    thumbnail_url: str


class SearchMeta(BaseModel):
    """Metadata for search results."""
    query: str
    count: int


class SearchResponse(BaseModel):
    """Response model for search endpoint (songs only)."""
    songs: List[Song]
    meta: SearchMeta


class UnifiedSearchResponse(BaseModel):
    """Response model for unified search endpoint (songs + playlists + albums + artists)."""
    songs: List[Song]
    playlists: List[Playlist]
    albums: List[Album] = []
    artists: List[Artist] = []
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
    lyrics: Optional[Any] = None  # LyricsData dict or None


class RelatedResponse(BaseModel):
    """Response model for related songs endpoint."""
    songs: List[Song]
