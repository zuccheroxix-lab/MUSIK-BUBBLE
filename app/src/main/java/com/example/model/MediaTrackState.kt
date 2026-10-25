package com.example.model

import android.graphics.Bitmap
import android.net.Uri

/**
 * Snapshot of current active media session playback and metadata.
 */
data class MediaTrackState(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val albumArt: Bitmap? = null,
    val albumArtUri: Uri? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val packageName: String = "",
    val appName: String = "",
    val hasActiveSession: Boolean = false,
    val canSkipNext: Boolean = true,
    val canSkipPrev: Boolean = true
)
