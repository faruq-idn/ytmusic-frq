# YT Music Personal App - Technical Documentation

## Architecture Overview

This app follows **Clean Architecture** with three main layers:

```
┌─────────────────────────────────────────────────┐
│                 PRESENTATION                     │
│  (Compose UI, ViewModels, State Management)     │
├─────────────────────────────────────────────────┤
│                    DOMAIN                        │
│  (UseCases, Models, Repository Interfaces)      │
├─────────────────────────────────────────────────┤
│                     DATA                         │
│  (Repository Impl, API, Database, DTOs)         │
└─────────────────────────────────────────────────┘
```

---

## Backend API

### Base URL
```
http://{YOUR_IP}:8000
```

### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/search?q={query}` | Search songs |
| GET | `/api/v1/stream/{video_id}` | Get stream URL |
| GET | `/api/v1/metadata/{video_id}` | Get song metadata & lyrics |
| GET | `/api/v1/related/{video_id}` | Get related songs |

### Response Format
```json
{
  "success": true,
  "data": { ... }
}
```

---

## Android Modules

### DI Modules (Hilt)

| Module | Provides |
|--------|----------|
| `AppModule` | Application context |
| `NetworkModule` | OkHttp, Retrofit, API |
| `DatabaseModule` | Room Database, DAOs |
| `RepositoryModule` | Repository bindings |
| `ImageModule` | Coil ImageLoader |

### Data Flow

```
UI (Compose) → ViewModel → UseCase → Repository → API/Database
     ↑                                    ↓
     └──────────── StateFlow ─────────────┘
```

---

## Key Components

### 1. PlayerViewModel
Central state management for audio playback.

**State:** `PlayerState`
- `currentSong` - Currently playing song
- `isPlaying` - Playback state
- `currentPosition` / `duration` - Progress
- `queue` / `currentIndex` - Queue management
- `isShuffleEnabled` / `repeatMode` - Playback modes

**Actions:**
- `playSong(song)` - Play single song
- `playSongFromList(songs, index)` - Play from list
- `togglePlayPause()` - Toggle playback
- `playNext()` / `playPrevious()` - Queue navigation
- `toggleShuffle()` / `toggleRepeat()` - Mode toggles
- `toggleFavorite()` - Add/remove favorite
- `seekTo(progress)` - Seek position

### 2. MusicPlaybackService
MediaSessionService for background playback.

**Features:**
- Background audio playback
- Media notification
- MediaSession integration
- Lock screen controls

### 3. MusicServiceConnection
Manages MediaController connection.

```kotlin
val controller: StateFlow<MediaController?>
fun connect()
fun disconnect()
```

### 4. QueueManager
Queue state management.

```kotlin
fun setQueue(songs: List<Song>, startIndex: Int)
fun next(): Song?
fun previous(): Song?
fun addToQueue(song: Song)
```

---

## Database Schema

### FavoriteSongEntity
```kotlin
@Entity(tableName = "favorite_songs")
data class FavoriteSongEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String?,
    val duration: String?,
    val addedAt: Long
)
```

---

## Image Caching

Coil configuration in `ImageModule`:
- **Memory Cache:** 25% of available RAM
- **Disk Cache:** 100MB in `cache/image_cache`
- **Crossfade:** 300ms animation

---

## Navigation

| Screen | Route | Description |
|--------|-------|-------------|
| Search | `search` | Main search screen |
| Library | `library` | Favorites list |
| Player | (overlay) | Full-screen player |

---

## Building

### Debug APK
```bash
./gradlew assembleDebug
```

### Release APK
```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/`

---

## Troubleshooting

### Backend Issues

| Problem | Solution |
|---------|----------|
| `yt-dlp` extraction fails | Run `pip install -U yt-dlp` |
| Connection refused | Check firewall, use correct IP |
| Port already in use | Change port: `--port 8001` |

### Android Issues

| Problem | Solution |
|---------|----------|
| Build fails | Sync Gradle, check SDK version |
| No songs found | Verify backend is running |
| Audio not playing | Check notification permission |
| Thumbnails not loading | Verify network access |

---

## Future Improvements

- [ ] Lyrics display in player
- [ ] Download for offline
- [ ] Playlist management
- [ ] Sleep timer
- [ ] Equalizer
- [ ] Widget support

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | Dec 2024 | Initial release |

---

*Last updated: December 2024*
