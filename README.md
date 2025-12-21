# YT Music Personal App

Personal music streaming app using YouTube Music as source.

## Features

- 🔍 **Search** - Search songs from YouTube Music
- 🎵 **Playback** - Stream audio directly
- 🔔 **Background Play** - Continue playing when app minimized
- 📱 **Media Controls** - Notification controls & lock screen

## Tech Stack

### Backend
- Python 3.11+
- FastAPI
- yt-dlp

### Android
- Kotlin
- Jetpack Compose
- Media3 ExoPlayer
- Hilt (DI)
- Retrofit

## Setup

### Backend
```bash
cd backend
python -m venv venv
.\venv\Scripts\activate  # Windows
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

### Android
1. Open project in Android Studio
2. Update `BASE_URL` in `app/build.gradle.kts` to your backend IP
3. Build and run on device

## Project Structure

```
ytmusic-frq/
├── backend/           # FastAPI backend
│   ├── app/
│   │   ├── main.py
│   │   ├── routers/
│   │   └── services/
│   └── requirements.txt
│
├── app/               # Android app
│   └── src/main/java/com/frq/ytmusic/
│       ├── data/          # Repository, API, DTOs
│       ├── domain/        # Models, UseCases
│       ├── presentation/  # ViewModels, Screens
│       ├── service/       # Background service
│       └── di/            # Hilt modules
│
└── gradle/
```

## License

MIT License - See [LICENSE](LICENSE)

## Disclaimer

This project is for personal/educational use only.
