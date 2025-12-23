"""
Album model for YouTube Music albums.
"""
from pydantic import BaseModel
from typing import Optional


class Album(BaseModel):
    """Represents an album from YouTube Music."""
    browse_id: str
    title: str
    artist: Optional[str] = None
    thumbnail_url: str
    year: Optional[str] = None
    is_explicit: bool = False
