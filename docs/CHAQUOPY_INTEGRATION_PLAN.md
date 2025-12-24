# Rencana Integrasi Chaquopy (Python di Android)

> **Status:** 📋 Tersimpan untuk implementasi nanti
> **Estimasi:** 4-7 hari development
> **Prasyarat:** Selesaikan fitur download terlebih dahulu

---

## Tujuan
Mengintegrasikan kode backend Python ke dalam APK Android menggunakan Chaquopy SDK, sehingga tidak perlu server eksternal.

## Konfigurasi

- **ABI:** arm64-v8a only (~55-75 MB APK)
- **Python:** 3.11
- **Dependencies:** ytmusicapi, yt-dlp, FFmpeg-Kit

---

## Setup Gradle

### settings.gradle.kts
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://chaquo.com/maven-test") }
    }
}
```

### build.gradle.kts (project)
```kotlin
plugins {
    id("com.chaquo.python") version "17.0.0" apply false
}
```

### build.gradle.kts (app)
```kotlin
plugins {
    id("com.chaquo.python")
}

android {
    defaultConfig {
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }
}

chaquopy {
    defaultConfig {
        version = "3.11"
        buildPython("python")  // atau path lengkap
        pip {
            install("ytmusicapi")
            install("yt-dlp")
            install("requests")
        }
    }
}

dependencies {
    implementation("com.arthenica:ffmpeg-kit-audio:6.0")
}
```

---

## Struktur File Python

```
app/src/main/python/
├── youtube_music.py      # Search, playlist, album, artist, lyrics
├── stream_extractor.py   # Extract audio URL
├── models/
│   ├── song.py
│   ├── playlist.py
│   ├── album.py
│   ├── artist.py
│   └── lyrics.py
└── utils/
    └── thumbnail.py
```

---

## Inisialisasi Python di App

```kotlin
// YtMusicApplication.kt
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class YtMusicApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
    }
}
```

---

## Contoh Panggilan Python dari Kotlin

```kotlin
class MusicRepository @Inject constructor() {
    private val python = Python.getInstance()
    private val ytMusic = python.getModule("youtube_music")
    
    suspend fun searchSongs(query: String): List<Song> = 
        withContext(Dispatchers.IO) {
            val result = ytMusic.callAttr("search_songs", query)
            result.asList().map { pyDict ->
                Song(
                    videoId = pyDict["video_id"].toString(),
                    title = pyDict["title"].toString(),
                    artist = pyDict["artist"].toString(),
                    thumbnailUrl = pyDict["thumbnail_url"].toString()
                )
            }
        }
}
```

---

## Yang Perlu Dimodifikasi

### Python (dari backend/)
- Hapus FastAPI imports dan decorators
- Jadikan plain functions yang return dict/list

### Kotlin
- `YtMusicApplication.kt` - inisialisasi Python
- `MusicRepository.kt` - ganti Retrofit → Python calls
- `NetworkModule.kt` - hapus YtMusicApi Retrofit

### Yang Tidak Berubah
- Semua UI/Screen
- Semua ViewModel
- MusicPlaybackService
- Room Database

---

## Timeline

| Fase | Durasi |
|------|--------|
| Setup Chaquopy + FFmpeg | 1 hari |
| Migrasi Python | 1 hari |
| Kotlin Bridge | 1-2 hari |
| Testing | 1-2 hari |
| **Total** | **4-7 hari** |

---

## Referensi
- [Chaquopy Documentation](https://chaquo.com/chaquopy/doc/current/)
- [FFmpeg-Kit](https://github.com/arthenica/ffmpeg-kit)
