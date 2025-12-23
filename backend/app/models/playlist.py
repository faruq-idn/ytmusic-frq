"""
Playlist model for YouTube Music playlists.
"""
from pydantic import BaseModel
from typing import Optional


class Playlist(BaseModel):
    """Represents a playlist from YouTube Music."""
    playlist_id: str
    title: str
    description: Optional[str] = None
    thumbnail_url: str
    song_count: Optional[int] = None
    author: Optional[str] = None
