"""
Search router - handles song search endpoints.
"""
from fastapi import APIRouter, Query, HTTPException

from app.models.response import ApiResponse
from app.models.song import SearchResponse, SearchMeta
from app.services.youtube_music import yt_music_service


router = APIRouter()


@router.get("/search", response_model=ApiResponse[SearchResponse])
async def search_songs(
    q: str = Query(..., min_length=1, description="Search query"),
    limit: int = Query(20, ge=1, le=50, description="Maximum number of results")
):
    """
    Search for songs on YouTube Music.
    
    - **q**: Search query (required)
    - **limit**: Max results (1-50, default 20)
    
    Returns list of songs with video_id, title, artist, album, duration, and thumbnail.
    """
    try:
        songs = yt_music_service.search_songs(q, limit)
        
        return ApiResponse(
            success=True,
            data=SearchResponse(
                songs=songs,
                meta=SearchMeta(query=q, count=len(songs))
            )
        )
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail={"code": "SEARCH_FAILED", "message": str(e)}
        )
