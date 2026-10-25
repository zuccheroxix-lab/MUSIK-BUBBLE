package com.example.service

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.net.Uri
import android.util.Log
import android.view.KeyEvent
import com.example.model.MediaTrackState
import com.example.player.LocalAudioPlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object MediaSessionHub {
    private const val TAG = "MediaSessionHub"

    private val _mediaState = MutableStateFlow(MediaTrackState())
    val mediaState: StateFlow<MediaTrackState> = _mediaState.asStateFlow()

    private val _isOverlayActive = MutableStateFlow(false)
    val isOverlayActive: StateFlow<Boolean> = _isOverlayActive.asStateFlow()

    private val _isNotificationServiceConnected = MutableStateFlow(false)
    val isNotificationServiceConnected: StateFlow<Boolean> = _isNotificationServiceConnected.asStateFlow()

    @Volatile
    private var isLocalPlaybackActive = false

    @Volatile
    private var activeController: MediaController? = null

    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            if (!isLocalPlaybackActive) {
                updateFromController(activeController)
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            if (!isLocalPlaybackActive) {
                updateFromController(activeController)
            }
        }

        override fun onSessionDestroyed() {
            Log.d(TAG, "Current active media session destroyed")
            activeController?.unregisterCallback(this)
            activeController = null
            if (!isLocalPlaybackActive) {
                _mediaState.value = MediaTrackState(hasActiveSession = false)
            }
        }
    }

    fun setOverlayActive(active: Boolean) {
        _isOverlayActive.value = active
    }

    fun setNotificationServiceConnected(connected: Boolean) {
        _isNotificationServiceConnected.value = connected
        if (!connected && !isLocalPlaybackActive) {
            detachController()
        }
    }

    fun setLocalPlaybackState(state: MediaTrackState) {
        isLocalPlaybackActive = true
        _mediaState.value = state
    }

    @Synchronized
    fun attachController(controller: MediaController?, context: Context?) {
        if (isLocalPlaybackActive && LocalAudioPlayerManager.isPlaying.value) {
            // Keep local playback prioritized while running
            activeController = controller
            return
        }

        isLocalPlaybackActive = false
        if (activeController == controller && controller != null) {
            updateFromController(controller, context)
            return
        }

        try {
            activeController?.unregisterCallback(controllerCallback)
        } catch (e: Exception) {
            Log.w(TAG, "Error unregistering previous controller callback", e)
        }

        activeController = controller
        if (controller != null) {
            try {
                controller.registerCallback(controllerCallback)
            } catch (e: Exception) {
                Log.w(TAG, "Error registering controller callback", e)
            }
            updateFromController(controller, context)
        } else {
            _mediaState.value = MediaTrackState(hasActiveSession = false)
        }
    }

    @Synchronized
    fun detachController() {
        try {
            activeController?.unregisterCallback(controllerCallback)
        } catch (_: Exception) {}
        activeController = null
        if (!isLocalPlaybackActive) {
            _mediaState.value = MediaTrackState(hasActiveSession = false)
        }
    }

    fun updateFromController(controller: MediaController?, context: Context? = null) {
        val current = controller ?: activeController
        if (current == null) {
            if (!isLocalPlaybackActive) {
                _mediaState.value = MediaTrackState(hasActiveSession = false)
            }
            return
        }

        isLocalPlaybackActive = false
        val metadata = current.metadata
        val playbackState = current.playbackState

        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING ||
                playbackState?.state == PlaybackState.STATE_FAST_FORWARDING ||
                playbackState?.state == PlaybackState.STATE_REWINDING

        var title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: ""

        var artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_AUTHOR)
            ?: ""

        val album = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: ""
        val duration = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L

        var artBitmap: Bitmap? = null
        try {
            artBitmap = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
        } catch (e: Exception) {
            Log.w(TAG, "Could not extract album art bitmap", e)
        }

        var artUri: Uri? = null
        val artUriString = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ART_URI)
        if (!artUriString.isNullOrBlank()) {
            try {
                artUri = Uri.parse(artUriString)
            } catch (_: Exception) {}
        }

        val pkgName = current.packageName ?: ""
        var appName = pkgName
        if (context != null && pkgName.isNotEmpty()) {
            try {
                val pm = context.packageManager
                val appInfo = pm.getApplicationInfo(pkgName, 0)
                appName = pm.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {}
        }

        val actions = playbackState?.actions ?: 0L
        val canSkipNext = (actions and PlaybackState.ACTION_SKIP_TO_NEXT) != 0L || actions == 0L
        val canSkipPrev = (actions and PlaybackState.ACTION_SKIP_TO_PREVIOUS) != 0L || actions == 0L
        val position = playbackState?.position ?: 0L

        if (title.isBlank() && appName.isNotBlank()) {
            title = appName
        }

        _mediaState.value = MediaTrackState(
            title = title,
            artist = artist,
            album = album,
            albumArt = artBitmap,
            albumArtUri = artUri,
            isPlaying = isPlaying,
            positionMs = position,
            durationMs = duration,
            packageName = pkgName,
            appName = appName,
            hasActiveSession = true,
            canSkipNext = canSkipNext,
            canSkipPrev = canSkipPrev
        )
    }

    fun play() {
        if (isLocalPlaybackActive) {
            LocalAudioPlayerManager.play()
            return
        }
        val controller = activeController ?: return
        try {
            controller.transportControls?.play()
        } catch (e: Exception) {
            Log.w(TAG, "Error invoking transport play", e)
            sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY)
        }
    }

    fun pause() {
        if (isLocalPlaybackActive) {
            LocalAudioPlayerManager.pause()
            return
        }
        val controller = activeController ?: return
        try {
            controller.transportControls?.pause()
        } catch (e: Exception) {
            Log.w(TAG, "Error invoking transport pause", e)
            sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PAUSE)
        }
    }

    fun togglePlayPause() {
        if (isLocalPlaybackActive) {
            LocalAudioPlayerManager.togglePlayPause()
            return
        }
        val controller = activeController ?: return
        val currentPlaying = _mediaState.value.isPlaying
        try {
            if (currentPlaying) {
                controller.transportControls?.pause()
            } else {
                controller.transportControls?.play()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error toggling play/pause", e)
            sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        }
    }

    fun skipToNext() {
        if (isLocalPlaybackActive) {
            LocalAudioPlayerManager.skipNext()
            return
        }
        val controller = activeController ?: return
        try {
            controller.transportControls?.skipToNext()
        } catch (e: Exception) {
            Log.w(TAG, "Error skipping to next", e)
            sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT)
        }
    }

    fun skipToPrevious() {
        if (isLocalPlaybackActive) {
            LocalAudioPlayerManager.skipPrevious()
            return
        }
        val controller = activeController ?: return
        try {
            controller.transportControls?.skipToPrevious()
        } catch (e: Exception) {
            Log.w(TAG, "Error skipping to previous", e)
            sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        }
    }

    fun seekTo(positionMs: Long) {
        if (isLocalPlaybackActive) {
            LocalAudioPlayerManager.seekTo(positionMs)
            return
        }
        val controller = activeController ?: return
        try {
            controller.transportControls?.seekTo(positionMs)
        } catch (e: Exception) {
            Log.w(TAG, "Error seeking position", e)
        }
    }

    private fun sendMediaKeyEvent(keyCode: Int) {
        val controller = activeController ?: return
        try {
            val eventDown = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
            val eventUp = KeyEvent(KeyEvent.ACTION_UP, keyCode)
            controller.dispatchMediaButtonEvent(eventDown)
            controller.dispatchMediaButtonEvent(eventUp)
        } catch (e: Exception) {
            Log.w(TAG, "Error dispatching media key event: $keyCode", e)
        }
    }
}
