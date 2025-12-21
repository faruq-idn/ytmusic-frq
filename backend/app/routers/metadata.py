"""
Metadata router for song information and lyrics.
"""
from fastapi import APIRouter, HTTPException

from app.models.response import ApiResponse
from app.models.song import MetadataResponse
from app.services.youtube_music import yt_music_service

router = APIRouter(prefix="/api/v1", tags=["metadata"])


@router.get("/metadata/{video_id}", response_model=ApiResponse)
async def get_metadata(video_id: str):
    """
    Get metadata for a song including lyrics if available.
    
    Args:
        video_id: YouTube video ID (11 characters)
    
    Returns:
        MetadataResponse with song info and lyrics
    """
    if len(video_id) != 11:
        raise HTTPException(status_code=400, detail="Invalid video ID format")
    
    # Get basic metadata
    metadata = yt_music_service.get_song_metadata(video_id)
    
    if not metadata:
        raise HTTPException(status_code=404, detail="Song not found")
    
    # Get lyrics (now returns structured LyricsData)
    lyrics = yt_music_service.get_lyrics(video_id)
    
    # Extract info from metadata
    video_details = metadata.get("videoDetails", {})
    
    response_data = MetadataResponse(
        video_id=video_id,
        title=video_details.get("title", "Unknown"),
        artist=video_details.get("author", "Unknown Artist"),
        album=None,  # Not always available in get_song response
        duration_seconds=int(video_details.get("lengthSeconds", 0)),
        has_lyrics=lyrics is not None,
        lyrics=lyrics.model_dump() if lyrics else None
    )
    
    return ApiResponse(success=True, data=response_data.model_dump())


@router.get("/related/{video_id}", response_model=ApiResponse)
async def get_related(video_id: str, limit: int = 20):
    """
    Get related/recommended songs based on a video.
    
    Args:
        video_id: YouTube video ID
        limit: Maximum number of results (default 20)
    
    Returns:
        List of related songs
    """
    if len(video_id) != 11:
        raise HTTPException(status_code=400, detail="Invalid video ID format")
    
    songs = yt_music_service.get_related_songs(video_id, limit)
    
    return ApiResponse(
        success=True,
        data={
            "songs": [song.model_dump() for song in songs]
        }
    )
