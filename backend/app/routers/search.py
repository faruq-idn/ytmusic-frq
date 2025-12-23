"""
Search router - handles song and playlist search endpoints.
"""
from fastapi import APIRouter, Query, HTTPException
from typing import Literal

from app.models.response import ApiResponse
from app.models.song import SearchResponse, SearchMeta, UnifiedSearchResponse
from app.services.youtube_music import yt_music_service


router = APIRouter()


@router.get("/search", response_model=ApiResponse[UnifiedSearchResponse])
async def search(
    q: str = Query(..., min_length=1, description="Search query"),
    limit: int = Query(20, ge=1, le=50, description="Maximum number of song results"),
    type: Literal["all", "songs", "playlists"] = Query("all", description="Type of results")
):
    """
    Search for content on YouTube Music.
    
    - **q**: Search query (required)
    - **limit**: Max song results (1-50, default 20)
    - **type**: Result type - "all", "songs", or "playlists" (default: all)
    
    Returns list of songs and/or playlists based on type parameter.
    """
    try:
        songs = []
        playlists = []
        
        if type in ["all", "songs"]:
            songs = yt_music_service.search_songs(q, limit)
        
        if type in ["all", "playlists"]:
            playlist_limit = 5 if type == "all" else limit
            playlists = yt_music_service.search_playlists(q, playlist_limit)
        
        total_count = len(songs) + len(playlists)
        
        return ApiResponse(
            success=True,
            data=UnifiedSearchResponse(
                songs=songs,
                playlists=playlists,
                meta=SearchMeta(query=q, count=total_count)
            )
        )
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail={"code": "SEARCH_FAILED", "message": str(e)}
        )


@router.get("/search/suggestions", response_model=ApiResponse[list])
async def get_suggestions(
    q: str = Query(..., min_length=2, description="Query for suggestions")
):
    """
    Get search suggestions for autocomplete.
    
    - **q**: Partial search query (min 2 chars)
    
    Returns list of suggestion strings.
    """
    try:
        suggestions = yt_music_service.get_search_suggestions(q)
        return ApiResponse(
            success=True,
            data=suggestions
        )
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail={"code": "SUGGESTIONS_FAILED", "message": str(e)}
        )

