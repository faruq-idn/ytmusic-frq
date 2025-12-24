"""
YT Music API - FastAPI Application Entry Point.
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings
from app.routers import search, stream, metadata, download, playlist, album, artist


# Create FastAPI application
app = FastAPI(
    title="YT Music API",
    description="Backend API untuk aplikasi YT Music Personal",
    version="1.0.0",
    debug=settings.debug
)

# CORS Middleware - untuk mengizinkan request dari Android app
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins.split(","),
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(search.router, prefix="/api/v1", tags=["Search"])
app.include_router(stream.router, prefix="/api/v1", tags=["Stream"])
app.include_router(playlist.router, prefix="/api/v1", tags=["Playlist"])
app.include_router(album.router, prefix="/api/v1", tags=["Album"])
app.include_router(artist.router, prefix="/api/v1", tags=["Artist"])
app.include_router(metadata.router, tags=["Metadata"])
app.include_router(download.router, tags=["Download"])


@app.get("/health", tags=["Health"])
async def health_check():
    """Health check endpoint."""
    return {
        "status": "ok",
        "version": "1.0.0",
        "message": "YT Music API is running"
    }


@app.get("/", tags=["Root"])
async def root():
    """Root endpoint with API info."""
    return {
        "name": "YT Music API",
        "version": "1.0.0",
        "docs": "/docs",
        "health": "/health"
    }
