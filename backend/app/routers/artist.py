"""
Artist router - handles artist detail endpoint.
"""
from fastapi import APIRouter, HTTPException

from app.models.response import ApiResponse
from app.services.youtube_music import yt_music_service


router = APIRouter()


@router.get("/artist/{browse_id}", response_model=ApiResponse)
async def get_artist(browse_id: str):
    """
    Get artist details including top songs and albums.
    
    - **browse_id**: YouTube Music artist browse ID (required)
    
    Returns artist info, top songs, and albums.
    """
    if not browse_id:
        raise HTTPException(status_code=400, detail="Artist ID required")
    
    artist_data = yt_music_service.get_artist(browse_id)
    
    if not artist_data:
        raise HTTPException(status_code=404, detail="Artist not found")
    
    # Convert Song and Album objects to dicts
    songs_data = [song.model_dump() for song in artist_data["songs"]]
    albums_data = [album.model_dump() for album in artist_data["albums"]]
    
    return ApiResponse(
        success=True,
        data={
            "browse_id": artist_data["browse_id"],
            "name": artist_data["name"],
            "thumbnail_url": artist_data["thumbnail_url"],
            "description": artist_data["description"],
            "subscribers": artist_data["subscribers"],
            "songs": songs_data,
            "albums": albums_data
        }
    )
