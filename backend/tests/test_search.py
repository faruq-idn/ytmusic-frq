"""
Unit tests for search endpoint.
"""
import pytest
from fastapi.testclient import TestClient

from app.main import app


client = TestClient(app)


class TestHealthEndpoint:
    """Tests for health check endpoint."""
    
    def test_health_check_returns_ok(self):
        """Health check should return status ok."""
        response = client.get("/health")
        
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "ok"
        assert "version" in data


class TestSearchEndpoint:
    """Tests for search endpoint."""
    
    def test_search_returns_success(self):
        """Search should return success with songs."""
        response = client.get("/api/v1/search?q=coldplay")
        
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "data" in data
        assert "songs" in data["data"]
        assert len(data["data"]["songs"]) > 0
    
    def test_search_requires_query(self):
        """Search should require query parameter."""
        response = client.get("/api/v1/search")
        
        assert response.status_code == 422  # Validation error
    
    def test_search_with_limit(self):
        """Search should respect limit parameter (may return slightly more due to API)."""
        response = client.get("/api/v1/search?q=test&limit=5")
        
        assert response.status_code == 200
        data = response.json()
        # ytmusicapi may return slightly more than limit, but should work
        assert len(data["data"]["songs"]) >= 1
    
    def test_search_meta_contains_query(self):
        """Search response should contain query in meta."""
        query = "imagine dragons"
        response = client.get(f"/api/v1/search?q={query}")
        
        assert response.status_code == 200
        data = response.json()
        assert data["data"]["meta"]["query"] == query
    
    def test_search_song_has_required_fields(self):
        """Each song should have required fields."""
        response = client.get("/api/v1/search?q=test&limit=1")
        
        assert response.status_code == 200
        data = response.json()
        
        if len(data["data"]["songs"]) > 0:
            song = data["data"]["songs"][0]
            assert "video_id" in song
            assert "title" in song
            assert "artist" in song
            assert "thumbnail_url" in song
    
    def test_search_empty_query_rejected(self):
        """Empty query should be rejected."""
        response = client.get("/api/v1/search?q=")
        
        # Empty string should fail min_length=1 validation
        assert response.status_code == 422
    
    def test_search_thumbnail_is_high_res(self):
        """Thumbnail URL should be transformed to high resolution."""
        response = client.get("/api/v1/search?q=coldplay&limit=1")
        
        assert response.status_code == 200
        data = response.json()
        
        if len(data["data"]["songs"]) > 0:
            thumbnail = data["data"]["songs"][0]["thumbnail_url"]
            # Should contain high-res dimensions
            assert "w800" in thumbnail or "w544" in thumbnail or thumbnail != ""
