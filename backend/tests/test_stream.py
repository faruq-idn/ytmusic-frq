"""
Unit tests for stream endpoint.
"""
import pytest
from fastapi.testclient import TestClient

from app.main import app


client = TestClient(app)


class TestStreamEndpoint:
    """Tests for stream endpoint."""
    
    @pytest.fixture
    def valid_video_id(self):
        """Get a valid video ID from search."""
        response = client.get("/api/v1/search?q=coldplay&limit=1")
        assert response.status_code == 200
        data = response.json()
        return data["data"]["songs"][0]["video_id"]
    
    def test_stream_returns_url(self, valid_video_id):
        """Stream should return a valid stream URL."""
        response = client.get(f"/api/v1/stream/{valid_video_id}")
        
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "data" in data
        assert "stream_url" in data["data"]
        assert data["data"]["stream_url"].startswith("http")
    
    def test_stream_has_required_fields(self, valid_video_id):
        """Stream response should have all required fields."""
        response = client.get(f"/api/v1/stream/{valid_video_id}")
        
        assert response.status_code == 200
        data = response.json()["data"]
        
        assert "video_id" in data
        assert "stream_url" in data
        assert "format" in data
        assert "expires_in_seconds" in data
        assert data["video_id"] == valid_video_id
    
    def test_stream_format_is_audio(self, valid_video_id):
        """Stream format should be audio format."""
        response = client.get(f"/api/v1/stream/{valid_video_id}")
        
        assert response.status_code == 200
        data = response.json()["data"]
        
        # Should be m4a, webm, or similar audio format
        assert data["format"] in ["m4a", "webm", "mp4", "opus"]
    
    def test_stream_invalid_length_rejected(self):
        """Video ID with wrong length should be rejected."""
        response = client.get("/api/v1/stream/short")
        
        assert response.status_code == 422  # Validation error
    
    def test_stream_too_long_rejected(self):
        """Video ID that's too long should be rejected."""
        response = client.get("/api/v1/stream/waytoolongvideoid123")
        
        assert response.status_code == 422  # Validation error
    
    def test_stream_expires_in_reasonable_time(self, valid_video_id):
        """Stream expiry should be reasonable (few hours)."""
        response = client.get(f"/api/v1/stream/{valid_video_id}")
        
        assert response.status_code == 200
        data = response.json()["data"]
        
        # Should expire in less than 24 hours
        assert data["expires_in_seconds"] <= 86400
        # Should be at least 1 hour
        assert data["expires_in_seconds"] >= 3600
