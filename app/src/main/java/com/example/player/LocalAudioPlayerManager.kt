package com.example.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.model.MediaTrackState
import com.example.service.MediaSessionHub
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class LocalAudioTrack(
    val id: String,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val albumArt: Bitmap? = null
)

object LocalAudioPlayerManager {
    private const val TAG = "LocalAudioPlayer"

    private var mediaPlayer: MediaPlayer? = null
    private var appContext: Context? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _playlist = MutableStateFlow<List<LocalAudioTrack>>(emptyList())
    val playlist: StateFlow<List<LocalAudioTrack>> = _playlist.asStateFlow()

    private val _currentTrackIndex = MutableStateFlow(-1)
    val currentTrackIndex: StateFlow<Int> = _currentTrackIndex.asStateFlow()

    private val _currentTrack = MutableStateFlow<LocalAudioTrack?>(null)
    val currentTrack: StateFlow<LocalAudioTrack?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val progressUpdater = object : Runnable {
        override fun run() {
            mediaPlayer?.let { player ->
                if (_isPlaying.value) {
                    try {
                        val pos = player.currentPosition.toLong()
                        _currentPositionMs.value = pos
                    } catch (_: Exception) {}
                    mainHandler.postDelayed(this, 500)
                }
            }
        }
    }

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun addUrisToPlaylist(context: Context, uris: List<Uri>) {
        appContext = context.applicationContext
        val newTracks = mutableListOf<LocalAudioTrack>()

        for (uri in uris) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)

                var title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"
                val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Unknown Album"
                val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val duration = durStr?.toLongOrNull() ?: 0L

                val picBytes = retriever.embeddedPicture
                val art = if (picBytes != null) {
                    BitmapFactory.decodeByteArray(picBytes, 0, picBytes.size)
                } else null

                retriever.release()

                if (title.isNullOrBlank()) {
                    title = uri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.') ?: "Audio Track"
                }

                newTracks.add(
                    LocalAudioTrack(
                        id = uri.toString(),
                        uri = uri,
                        title = title,
                        artist = artist,
                        album = album,
                        durationMs = duration,
                        albumArt = art
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed to extract metadata for uri: $uri", e)
                val fallbackTitle = uri.lastPathSegment ?: "Local Audio Track"
                newTracks.add(
                    LocalAudioTrack(
                        id = uri.toString(),
                        uri = uri,
                        title = fallbackTitle,
                        artist = "Local File",
                        album = "Device Storage",
                        durationMs = 0L,
                        albumArt = null
                    )
                )
            }
        }

        val updated = _playlist.value.toMutableList().apply { addAll(newTracks) }
        _playlist.value = updated

        // If nothing is playing, load first new track
        if (_currentTrack.value == null && updated.isNotEmpty()) {
            playTrackAtIndex(0)
        }
    }

    fun playTrackAtIndex(index: Int) {
        val tracks = _playlist.value
        if (index !in tracks.indices) return

        val track = tracks[index]
        _currentTrackIndex.value = index
        _currentTrack.value = track
        _durationMs.value = track.durationMs
        _currentPositionMs.value = 0L

        playUri(track.uri)
        syncWithMediaSessionHub(track, true)
    }

    private fun playUri(uri: Uri) {
        val context = appContext ?: return
        stopCurrentPlayer()

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, uri)
                setOnPreparedListener { mp ->
                    mp.start()
                    _isPlaying.value = true
                    _durationMs.value = mp.duration.toLong()
                    mainHandler.post(progressUpdater)
                    _currentTrack.value?.let { syncWithMediaSessionHub(it, true) }
                }
                setOnCompletionListener {
                    skipNext()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    _isPlaying.value = false
                    mainHandler.removeCallbacks(progressUpdater)
                    // Auto-advance on error
                    skipNext()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MediaPlayer for uri: $uri", e)
            _isPlaying.value = false
        }
    }

    fun play() {
        mediaPlayer?.let { player ->
            try {
                if (!player.isPlaying) {
                    player.start()
                    _isPlaying.value = true
                    mainHandler.post(progressUpdater)
                    _currentTrack.value?.let { syncWithMediaSessionHub(it, true) }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error resuming player", e)
            }
        } ?: run {
            val idx = _currentTrackIndex.value
            if (idx != -1) playTrackAtIndex(idx)
        }
    }

    fun pause() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.pause()
                    _isPlaying.value = false
                    mainHandler.removeCallbacks(progressUpdater)
                    _currentTrack.value?.let { syncWithMediaSessionHub(it, false) }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error pausing player", e)
            }
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun skipNext() {
        val tracks = _playlist.value
        if (tracks.isEmpty()) return
        val nextIdx = (_currentTrackIndex.value + 1) % tracks.size
        playTrackAtIndex(nextIdx)
    }

    fun skipPrevious() {
        val tracks = _playlist.value
        if (tracks.isEmpty()) return
        val prevIdx = if (_currentTrackIndex.value - 1 < 0) tracks.size - 1 else _currentTrackIndex.value - 1
        playTrackAtIndex(prevIdx)
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { player ->
            try {
                player.seekTo(positionMs.toInt())
                _currentPositionMs.value = positionMs
            } catch (e: Exception) {
                Log.w(TAG, "Error seeking MediaPlayer", e)
            }
        }
    }

    fun clearPlaylist() {
        stopCurrentPlayer()
        _playlist.value = emptyList()
        _currentTrack.value = null
        _currentTrackIndex.value = -1
        _isPlaying.value = false
        _currentPositionMs.value = 0L
        _durationMs.value = 0L
    }

    private fun stopCurrentPlayer() {
        mainHandler.removeCallbacks(progressUpdater)
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _isPlaying.value = false
    }

    private fun syncWithMediaSessionHub(track: LocalAudioTrack, playing: Boolean) {
        // Forward local player playback state to MediaSessionHub for the Floating Overlay!
        MediaSessionHub.setLocalPlaybackState(
            MediaTrackState(
                title = track.title,
                artist = track.artist,
                album = track.album,
                albumArt = track.albumArt,
                isPlaying = playing,
                positionMs = _currentPositionMs.value,
                durationMs = track.durationMs,
                packageName = appContext?.packageName ?: "local.player",
                appName = "Local Player",
                hasActiveSession = true,
                canSkipNext = true,
                canSkipPrev = true
            )
        )
    }
}
