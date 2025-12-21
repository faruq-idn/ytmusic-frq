# YT Music API Backend

Backend API untuk aplikasi YT Music Personal menggunakan FastAPI.

## Tech Stack
- **FastAPI** - Modern async web framework
- **ytmusicapi** - YouTube Music data
- **yt-dlp** - Audio stream extraction
- **Pydantic** - Data validation

## Setup

```bash
# Create virtual environment
python -m venv venv

# Activate (Windows)
venv\Scripts\activate

# Activate (Linux/Mac)
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt
```

## Run Server

```bash
# Development (with auto-reload)
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

# Production
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/health` | GET | Health check |
| `/api/v1/search?q={query}` | GET | Search songs |
| `/api/v1/stream/{video_id}` | GET | Get audio stream URL |
| `/api/v1/metadata/{video_id}` | GET | Get song metadata |
| `/api/v1/related/{video_id}` | GET | Get related songs |

## Testing

```bash
# Run all tests
pytest tests/ -v

# Run specific test
pytest tests/test_search.py -v
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| HOST | 0.0.0.0 | Server host |
| PORT | 8000 | Server port |
| DEBUG | true | Debug mode |
| CORS_ORIGINS | * | Allowed origins |

## Project Structure

```
backend/
├── app/
│   ├── __init__.py
│   ├── main.py          # FastAPI app
│   ├── config.py        # Settings
│   ├── routers/         # API endpoints
│   ├── services/        # Business logic
│   ├── models/          # Pydantic models
│   └── utils/           # Helpers
├── tests/
├── requirements.txt
├── .env
└── README.md
```
