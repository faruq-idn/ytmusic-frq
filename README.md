# YT Music Personal App

🎵 Personal music streaming app powered by YouTube Music.

![Platform](https://img.shields.io/badge/Platform-Android-green)
![Backend](https://img.shields.io/badge/Backend-FastAPI-009688)
![License](https://img.shields.io/badge/License-MIT-blue)

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🔍 **Search** | Search songs from YouTube Music |
| 🎵 **Stream** | High-quality audio streaming |
| 🔔 **Background Play** | Keep playing when app minimized |
| 📱 **Media Controls** | Notification & lock screen controls |
| ⏭️ **Queue** | Next/Previous song navigation |
| 🔀 **Shuffle & Repeat** | Playback modes |
| ❤️ **Favorites** | Save songs locally |
| 🎨 **Related Songs** | Auto-queue similar songs |

## 🛠️ Tech Stack

### Backend
- **Python 3.11+** - Runtime
- **FastAPI** - API framework
- **yt-dlp** - YouTube extraction
- **ytmusicapi** - YouTube Music search

### Android
- **Kotlin** - Language
- **Jetpack Compose** - UI framework
- **Media3 ExoPlayer** - Audio playback
- **Hilt** - Dependency injection
- **Retrofit + OkHttp** - Networking
- **Room** - Local database
- **Coil** - Image loading

## 🚀 Quick Start

### Prerequisites
- Python 3.11+
- Android Studio Ladybug+
- Android device (API 26+)

### 1. Backend Setup
```bash
cd backend
python -m venv venv

# Windows
.\venv\Scripts\activate

# Linux/Mac
source venv/bin/activate

pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

### 2. Android Setup
1. Open project in Android Studio
2. Update `BASE_URL` in `app/build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "BASE_URL", "\"http://YOUR_IP:8000/\"")
   ```
3. Sync Gradle
4. Run on physical device

## 📁 Project Structure

```
ytmusic-frq/
├── 🐍 backend/
│   ├── app/
│   │   ├── main.py           # FastAPI entry
│   │   ├── routers/          # API endpoints
│   │   │   ├── search.py
│   │   │   ├── stream.py
│   │   │   └── metadata.py
│   │   ├── services/         # Business logic
│   │   └── models/           # Pydantic models
│   └── requirements.txt
│
├── 📱 app/src/main/java/com/frq/ytmusic/
│   ├── data/                 # Data layer
│   │   ├── remote/           # API, DTOs
│   │   ├── local/            # Room database
│   │   └── repository/       # Repository impl
│   │
│   ├── domain/               # Domain layer
│   │   ├── model/            # Domain models
│   │   ├── repository/       # Interfaces
│   │   └── usecase/          # Business logic
│   │
│   ├── presentation/         # UI layer
│   │   ├── search/           # Search screen
│   │   ├── library/          # Library screen
│   │   ├── player/           # Player UI
│   │   └── navigation/       # Navigation
│   │
│   ├── service/              # Background service
│   ├── di/                   # Hilt modules
│   └── ui/theme/             # Material theme
│
└── 📄 docs/
    └── DOCUMENTATION.md
```

## 📖 Documentation

See [DOCUMENTATION.md](DOCUMENTATION.md) for detailed technical documentation.

## 🔧 Configuration

| Variable | Location | Description |
|----------|----------|-------------|
| `BASE_URL` | `app/build.gradle.kts` | Backend URL |
| `--host` | Backend CLI | Bind address |
| `--port` | Backend CLI | Port number |

## 📝 License

MIT License - See [LICENSE](LICENSE)

## ⚠️ Disclaimer

This project is for **personal/educational use only**. 
Do not use for commercial purposes or redistribute content.

---

Made with ❤️ using Kotlin & Python
