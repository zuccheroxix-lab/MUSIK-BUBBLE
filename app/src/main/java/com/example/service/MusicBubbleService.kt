package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Foreground Service that hosts the Music Bubble floating overlay.
 */
class MusicBubbleService : Service() {

    companion object {
        private const val TAG = "MusicBubbleService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "music_bubble_overlay_channel"

        const val ACTION_START = "com.example.action.START_OVERLAY"
        const val ACTION_STOP = "com.example.action.STOP_OVERLAY"
        const val ACTION_TOGGLE_PLAY = "com.example.action.TOGGLE_PLAY"
        const val ACTION_NEXT = "com.example.action.NEXT"
        const val ACTION_PREV = "com.example.action.PREV"

        fun start(context: Context) {
            val intent = Intent(context, MusicBubbleService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, MusicBubbleService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var overlayManager: BubbleOverlayManager? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        overlayManager = BubbleOverlayManager(this) {
            stopSelf()
        }

        // Collect MediaSessionHub state updates to keep overlay in sync
        serviceScope.launch {
            MediaSessionHub.mediaState.collectLatest { state ->
                overlayManager?.updateMediaState(state)
                updateNotification(state.title, state.artist, state.isPlaying)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_PLAY -> {
                MediaSessionHub.togglePlayPause()
            }
            ACTION_NEXT -> {
                MediaSessionHub.skipToNext()
            }
            ACTION_PREV -> {
                MediaSessionHub.skipToPrevious()
            }
            else -> {
                // Check SYSTEM_ALERT_WINDOW permission
                if (Settings.canDrawOverlays(this)) {
                    val initialNotification = buildNotification("Music Bubble Active", "Tap to open app or manage overlay", false)
                    startForeground(NOTIFICATION_ID, initialNotification)
                    overlayManager?.show()
                    MediaSessionHub.setOverlayActive(true)
                } else {
                    Log.w(TAG, "Cannot start overlay: SYSTEM_ALERT_WINDOW not granted")
                    stopSelf()
                    return START_NOT_STICKY
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        overlayManager?.hide()
        overlayManager = null
        MediaSessionHub.setOverlayActive(false)
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_overlay_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_overlay_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, text: String, isPlaying: Boolean): Notification {
        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pAppIntent = PendingIntent.getActivity(
            this,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, MusicBubbleService::class.java).apply {
            action = ACTION_STOP
        }
        val pStopIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playIntent = Intent(this, MusicBubbleService::class.java).apply {
            action = ACTION_TOGGLE_PLAY
        }
        val pPlayIntent = PendingIntent.getService(
            this,
            2,
            playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playActionTitle = if (isPlaying) "Pause" else "Play"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pAppIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(playIcon, playActionTitle, pPlayIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.action_stop), pStopIntent)
            .build()
    }

    private fun updateNotification(trackTitle: String, artist: String, isPlaying: Boolean) {
        val title = if (trackTitle.isNotBlank()) trackTitle else getString(R.string.notification_title)
        val text = if (artist.isNotBlank()) artist else getString(R.string.notification_text)
        val notif = buildNotification(title, text, isPlaying)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, notif)
    }
}
