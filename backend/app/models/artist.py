"""
Artist model for YouTube Music artists.
"""
from pydantic import BaseModel
from typing import Optional


class Artist(BaseModel):
    """Represents an artist from YouTube Music."""
    browse_id: str
    name: str
    thumbnail_url: str
    subscribers: Optional[str] = None
