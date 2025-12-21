"""
Download router for audio files.
"""
import os
from fastapi import APIRouter, BackgroundTasks, HTTPException
from fastapi.responses import FileResponse

from app.services.youtube_music import yt_music_service

router = APIRouter(prefix="/api/v1", tags=["download"])


def cleanup_file(path: str):
    """Remove temporary file after download."""
    try:
        if os.path.exists(path):
            os.remove(path)
    except Exception as e:
        print(f"Error cleaning up {path}: {e}")


@router.get("/download/{video_id}")
async def download_audio(
    video_id: str, 
    quality: str = "best",
    background_tasks: BackgroundTasks = None
):
    """
    Download audio for a song.
    
    Args:
        video_id: YouTube video ID
        quality: Audio quality (best, 128k, 256k)
        
    Returns:
        File stream
    """
    if len(video_id) != 11:
        raise HTTPException(status_code=400, detail="Invalid video ID format")
    
    file_path = yt_music_service.download_audio(video_id, quality)
    
    if not file_path or not os.path.exists(file_path):
        raise HTTPException(status_code=500, detail="Download failed")
    
    # Schedule cleanup after response is sent
    # Note: FileResponse opens the file. BackgroundTasks run after response.
    if background_tasks:
        background_tasks.add_task(cleanup_file, file_path)
    
    return FileResponse(
        path=file_path, 
        filename=f"{video_id}.m4a",
        media_type="audio/mp4"
    )
