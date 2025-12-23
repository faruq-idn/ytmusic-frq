"""
Playlist router - handles playlist endpoints.
"""
from fastapi import APIRouter, HTTPException

from app.models.response import ApiResponse
from app.services.youtube_music import yt_music_service


router = APIRouter()


@router.get("/playlist/{playlist_id}", response_model=ApiResponse)
async def get_playlist(playlist_id: str):
    """
    Get playlist details including all songs.
    
    - **playlist_id**: YouTube Music playlist/browse ID
    
    Returns playlist info with list of songs.
    """
    if not playlist_id:
        raise HTTPException(status_code=400, detail="Playlist ID required")
    
    playlist_data = yt_music_service.get_playlist(playlist_id)
    
    if not playlist_data:
        raise HTTPException(status_code=404, detail="Playlist not found")
    
    # Convert Song objects to dicts
    songs_data = [song.model_dump() for song in playlist_data["songs"]]
    
    return ApiResponse(
        success=True,
        data={
            "playlist_id": playlist_data["playlist_id"],
            "title": playlist_data["title"],
            "description": playlist_data["description"],
            "thumbnail_url": playlist_data["thumbnail_url"],
            "author": playlist_data["author"],
            "song_count": playlist_data["song_count"],
            "songs": songs_data
        }
    )
