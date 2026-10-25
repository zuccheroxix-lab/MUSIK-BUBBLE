# Music Bubble Ultimate 🎵🫧

**Music Bubble** is a high-performance native Android music hub combining an interactive floating overlay player, official **Spotify Web API** catalog search & deep integration, and a full-featured **Local Audio Player** with Storage Access Framework (SAF) and embedded ID3 tag extraction.

---

## ✨ Core Features

### 1. 🫧 Floating Music Overlay (Native WindowManager)
- Draggable across any running app or home screen.
- Auto-magnetic snapping to device screen edges.
- Pitch Black & Burgundy glassmorphism UI with elegant Crimson Red neon accents.
- Single tap expands the bubble into the full-featured Mini Player.
- Real-time transparency & opacity sliders (40% to 100%) and bubble diameter (48dp – 76dp).
- Tactile haptic feedback on touch.
- Quick minimize and close controls.

### 2. 🟢 Spotify Web API Integration
- Real-time catalog search for tracks, albums, artists, and playlists.
- Filter chips: All, Tracks, Albums, Artists, Playlists.
- Official album artwork, artist rosters, and release information.
- One-tap deep linking to open and continue playback in the Spotify app (`spotify:track:...`).
- In-app Spotify Connect settings with token validation and credentials security.

### 3. 📁 Offline Local Music Player
- Pure on-device audio playback powered by Android `MediaPlayer` and `AudioAttributes`.
- Storage Access Framework (SAF) audio file picker (`audio/*`): MP3, FLAC, WAV, M4A, OGG.
- Real-time ID3 metadata reader (`MediaMetadataRetriever`): track title, artist, album, and embedded album art bitmap.
- Complete queue management: tap track to play, continuous seekbar, auto-advance on song completion.
- Seamless synchronization with the Floating Bubble Overlay!

### 4. ❤️ Library & Favorites
- Save favorite tracks from both Spotify search and local files.
- Listening and search history logging.
- Offline persistence via SharedPreferences JSON storage.

### 5. 🔐 Security, Permissions & Privacy
- Zero analytics, zero ad tracking, zero user data sent to external servers.
- `SYSTEM_ALERT_WINDOW`: to display floating overlay over other apps.
- `BIND_NOTIFICATION_LISTENER_SERVICE`: to connect to system `MediaSessionManager` for active third-party media players (Spotify, YouTube Music, etc.).
- `POST_NOTIFICATIONS`: Android 13+ foreground service status.

---

## 📱 Tech Stack & Architecture

- **Language**: Kotlin 2.2+
- **UI Framework**: Jetpack Compose (Material 3)
- **Networking**: Retrofit 2 + OkHttp 4 + Moshi
- **Image Loading**: Coil Compose
- **System Services**: `WindowManager`, `MediaSessionManager`, `NotificationListenerService`, `MediaPlayer`, `MediaMetadataRetriever`
- **Testing**: Robolectric + JUnit 4
- **CI/CD**: GitHub Actions

---

## 🛠️ Local Build Instructions

### Compile Debug APK:
```bash
./gradlew :app:assembleDebug
```
Output path: `app/build/outputs/apk/debug/app-debug.apk`

### Compile Release APK:
```bash
./gradlew :app:assembleRelease
```
Output path: `app/build/outputs/apk/release/app-release-unsigned.apk`

### Run Robolectric & Unit Tests:
```bash
./gradlew :app:testDebugUnitTest
```

---

## 🚀 GitHub Actions CI/CD Pipeline

The `.github/workflows/android-apk.yml` workflow automatically:
1. Checks out repository and configures JDK 17 (Temurin).
2. Executes all Robolectric and unit tests.
3. Compiles both Debug and Release APKs.
4. Verifies APK outputs and file sizes.
5. Uploads `music-bubble-debug-apk` and `music-bubble-release-apk` as workflow artifacts.
6. Automatically publishes a **GitHub Release** with APK assets when a version tag (`v*`) is pushed.
