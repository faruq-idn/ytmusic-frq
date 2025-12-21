"""
Stream router - handles audio stream URL extraction endpoints.
"""
from fastapi import APIRouter, HTTPException, Path

from app.models.response import ApiResponse
from app.models.song import StreamData
from app.services.stream_extractor import stream_extractor_service


router = APIRouter()


@router.get("/stream/{video_id}", response_model=ApiResponse[StreamData])
async def get_stream(
    video_id: str = Path(
        ..., 
        min_length=11, 
        max_length=11, 
        description="YouTube video ID (11 characters)"
    )
):
    """
    Get direct audio stream URL for a video.
    
    - **video_id**: YouTube video ID (exactly 11 characters)
    
    Returns stream URL, format, quality, and expiration time.
    
    Note: Stream URLs expire after ~6 hours.
    """
    try:
        stream_info = stream_extractor_service.get_stream_url(video_id)
        
        return ApiResponse(
            success=True,
            data=StreamData(
                video_id=video_id,
                stream_url=stream_info.url,
                format=stream_info.format,
                quality=stream_info.quality,
                expires_in_seconds=stream_info.expires_in_seconds
            )
        )
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail={"code": "STREAM_FAILED", "message": str(e)}
        )
