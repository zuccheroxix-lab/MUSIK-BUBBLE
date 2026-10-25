# Music Bubble 🎵🫧

**Music Bubble** is a modern, high-performance native Android application featuring a floating overlay mini player that hovers over games, social apps, and your home screen. It integrates directly with Android's native `MediaSessionManager` to provide real-time playback control, track metadata, and album art from any active audio player (Spotify, YouTube Music, Apple Music, VLC, Musicolet, etc.).

---

## ✨ Features

- **Floating Interactive Bubble**:
  - Touch-based dragging anywhere across the screen.
  - Magnetic edge-snapping to screen borders.
  - Glowing pitch black & burgundy aesthetic with elegant crimson accents.
  - Single tap expands into the full mini-player panel.
- **Real System Media Control (No Mocks)**:
  - Play, Pause, Next, and Previous track controls.
  - Real-time track title, artist name, and album artwork.
  - Active audio player source detection (e.g. Spotify, YouTube Music).
- **Customizable Overlay**:
  - Adjustable bubble diameter (48dp – 76dp) with live visual preview.
  - Real-time transparency & opacity sliders for bubble and mini-player card.
  - Haptic vibration feedback on tap.
  - Instant dismiss and collapse buttons.
- **Low-Memory & Battery Optimized**:
  - Engineered for low RAM devices: uses lightweight native views inside the `WindowManager` overlay.
  - Clean lifecycle management, avoiding memory leaks and battery drain.
- **100% Offline & Private**:
  - Zero analytics, zero ads, zero external server tracking. All media session synchronization is processed strictly on-device.

---

## 📱 Architecture & Android Components

| Component | Class / API | Responsibility |
|---|---|---|
| **Foreground Overlay Service** | `MusicBubbleService` | Manages the ongoing lifecycle of the floating WindowManager view with low-priority notification |
| **WindowManager Overlay** | `BubbleOverlayManager` | Renders the draggable floating bubble and collapsible mini-player panel (`TYPE_APPLICATION_OVERLAY`) |
| **Media Listener Service** | `MusicNotificationListenerService` | Binds to system `MediaSessionManager` to capture playback state and metadata from active media apps |
| **Media Session Hub** | `MediaSessionHub` | Centralized Kotlin `StateFlow` coordinator for active `MediaController` commands |
| **Dashboard & UI** | `MainActivity` | Jetpack Compose Material 3 interface with dark burgundy theme, status badges, and settings |

---

## 🔐 Required Permissions & Why

1. **Display Over Other Apps (`SYSTEM_ALERT_WINDOW`)**:
   - Required by Android to draw the floating bubble overlay over other running applications and games.
2. **Notification & Media Session Access (`BIND_NOTIFICATION_LISTENER_SERVICE`)**:
   - Required by Android's security architecture to query `MediaSessionManager.getActiveSessions()` and communicate with media controllers.
3. **Post Notifications (`POST_NOTIFICATIONS`)**:
   - Required on Android 13+ (API 33+) to show the ongoing foreground service notification that ensures the overlay is not unexpectedly killed by the OS.
4. **Battery Optimization Exemption (Optional but recommended)**:
   - Prevents aggressive manufacturer battery managers (MIUI, OneUI, ColorOS) from putting the service to sleep during long music sessions.

---

## 🛠️ Build & Compilation

### Requirements:
- JDK 17 (Eclipse Temurin or OpenJDK)
- Android SDK 36 (Build Tools 36.0.0, Platform Tools)
- Gradle 8.11+ / AGP 9.1+

### Build Debug APK:
```bash
./gradlew :app:assembleDebug
```
The debug APK will be created at:
`app/build/outputs/apk/debug/app-debug.apk`

### Build Release APK:
```bash
./gradlew :app:assembleRelease
```
The release APK will be created at:
`app/build/outputs/apk/release/app-release-unsigned.apk` (or signed if keystore configured)

### Run Unit & Robolectric Tests:
```bash
./gradlew :app:testDebugUnitTest
```

---

## 🚀 GitHub Actions CI/CD Pipeline

The project includes an automated GitHub Actions CI/CD workflow located at `.github/workflows/android-apk.yml`.

### Triggers:
- **Push & Pull Requests** on `main` and `master` branches: runs verification and compiles APKs.
- **Manual Trigger** via GitHub `workflow_dispatch`: triggers build on-demand.
- **Tag Push** (`v*`, e.g. `v1.0.0`): builds release assets and publishes a **GitHub Release**.

### Download Artifacts:
1. **Debug APK**:
   - Navigate to the **Actions** tab on your GitHub repository.
   - Click the latest successful workflow run.
   - Download the artifact named **`music-bubble-debug-apk`**.
2. **Release APK**:
   - For version releases, navigate to the **Releases** page on GitHub.
   - Download **`MusicBubble-release.apk`** directly from the Release Assets.

---

## 📲 Installation on Android Device

1. Download the generated `MusicBubble-debug.apk` to your phone.
2. Open the file and tap **Install** (allow "Install unknown apps" in Android settings if prompted).
3. Open **Music Bubble**.
4. Go to the **Permissions** tab:
   - Tap **Grant Overlay Permission** → enable "Allow display over other apps" for Music Bubble.
   - Tap **Enable Media Access** → toggle ON "Music Bubble Media Session Service".
5. Return to the **Home** tab and tap **START FLOATING BUBBLE**.
6. Play any song in Spotify, YouTube Music, or your favorite player — the bubble will reflect the song and let you control playback from anywhere!
