package com.example.service

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.util.Log

/**
 * Android NotificationListenerService that gains access to system MediaSessionManager
 * to detect and control active media players (Spotify, YouTube Music, etc.).
 */
class MusicNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "MusicNotifListener"
    }

    private var mediaSessionManager: MediaSessionManager? = null
    private var componentName: ComponentName? = null

    private val sessionsChangedListener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            Log.d(TAG, "Active media sessions count: ${controllers?.size ?: 0}")
            selectAndAttachController(controllers)
        }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "NotificationListenerService connected")
        MediaSessionHub.setNotificationServiceConnected(true)

        try {
            componentName = ComponentName(this, MusicNotificationListenerService::class.java)
            mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            mediaSessionManager?.let { msm ->
                componentName?.let { cn ->
                    msm.addOnActiveSessionsChangedListener(sessionsChangedListener, cn)
                    val activeSessions = msm.getActiveSessions(cn)
                    selectAndAttachController(activeSessions)
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException accessing MediaSessionManager. Permission might not be granted.", e)
            MediaSessionHub.setNotificationServiceConnected(false)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in onListenerConnected", e)
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "NotificationListenerService disconnected")
        cleanup()
        MediaSessionHub.setNotificationServiceConnected(false)
    }

    override fun onDestroy() {
        cleanup()
        MediaSessionHub.setNotificationServiceConnected(false)
        super.onDestroy()
    }

    private fun cleanup() {
        try {
            mediaSessionManager?.removeOnActiveSessionsChangedListener(sessionsChangedListener)
        } catch (e: Exception) {
            Log.w(TAG, "Error removing sessions changed listener", e)
        }
        mediaSessionManager = null
        componentName = null
    }

    private fun selectAndAttachController(controllers: List<MediaController>?) {
        if (controllers.isNullOrEmpty()) {
            MediaSessionHub.detachController()
            return
        }

        // Prefer the controller that is currently playing
        val playingController = controllers.firstOrNull {
            it.playbackState?.state == PlaybackState.STATE_PLAYING ||
                    it.playbackState?.state == PlaybackState.STATE_BUFFERING
        }

        val chosen = playingController ?: controllers.firstOrNull()
        MediaSessionHub.attachController(chosen, applicationContext)
    }
}
