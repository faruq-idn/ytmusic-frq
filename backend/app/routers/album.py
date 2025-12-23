"""
Album router - handles album endpoints.
"""
from fastapi import APIRouter, HTTPException

from app.models.response import ApiResponse
from app.services.youtube_music import yt_music_service


router = APIRouter()


@router.get("/album/{browse_id}", response_model=ApiResponse)
async def get_album(browse_id: str):
    """
    Get album details including all tracks.
    
    - **browse_id**: YouTube Music album browse ID
    
    Returns album info with list of songs.
    """
    if not browse_id:
        raise HTTPException(status_code=400, detail="Album ID required")
    
    album_data = yt_music_service.get_album(browse_id)
    
    if not album_data:
        raise HTTPException(status_code=404, detail="Album not found")
    
    # Convert Song objects to dicts
    songs_data = [song.model_dump() for song in album_data["songs"]]
    
    return ApiResponse(
        success=True,
        data={
            "browse_id": album_data["browse_id"],
            "title": album_data["title"],
            "artist": album_data["artist"],
            "thumbnail_url": album_data["thumbnail_url"],
            "year": album_data["year"],
            "track_count": album_data["track_count"],
            "duration": album_data["duration"],
            "songs": songs_data
        }
    )
