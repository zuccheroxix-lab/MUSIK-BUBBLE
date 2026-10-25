package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.MediaTrackState
import com.example.preferences.BubblePreferences
import com.example.preferences.LibraryStorage
import com.example.preferences.SavedTrack
import com.example.service.MediaSessionHub
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context validates app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Music Bubble", appName)
    }

    @Test
    fun `bubble preferences store and retrieve correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = BubblePreferences(context)

        assertEquals(BubblePreferences.DEFAULT_BUBBLE_SIZE, prefs.bubbleSizeDp)
        assertEquals(BubblePreferences.DEFAULT_BUBBLE_ALPHA, prefs.bubbleAlpha, 0.01f)
        assertTrue(prefs.autoSnap)
        assertTrue(prefs.vibrateOnTap)

        prefs.bubbleSizeDp = 72
        prefs.bubbleAlpha = 0.85f
        prefs.autoSnap = false

        assertEquals(72, prefs.bubbleSizeDp)
        assertEquals(0.85f, prefs.bubbleAlpha, 0.01f)
        assertFalse(prefs.autoSnap)
    }

    @Test
    fun `media track state model holds playback properties`() {
        val state = MediaTrackState(
            title = "Starboy",
            artist = "The Weeknd",
            album = "Starboy",
            isPlaying = true,
            packageName = "com.spotify.music",
            appName = "Spotify",
            hasActiveSession = true
        )

        assertEquals("Starboy", state.title)
        assertEquals("The Weeknd", state.artist)
        assertTrue(state.isPlaying)
        assertTrue(state.hasActiveSession)
        assertEquals("Spotify", state.appName)
    }

    @Test
    fun `media session hub toggles overlay status`() {
        MediaSessionHub.setOverlayActive(true)
        assertTrue(MediaSessionHub.isOverlayActive.value)

        MediaSessionHub.setOverlayActive(false)
        assertFalse(MediaSessionHub.isOverlayActive.value)
    }

    @Test
    fun `library storage saves and removes favorites correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val library = LibraryStorage(context)
        val track = SavedTrack(
            id = "track_123",
            title = "Blinding Lights",
            artist = "The Weeknd",
            album = "After Hours"
        )

        assertFalse(library.isFavorite("track_123"))
        library.toggleFavorite(track)
        assertTrue(library.isFavorite("track_123"))

        library.toggleFavorite(track)
        assertFalse(library.isFavorite("track_123"))
    }

    @Test
    fun `library storage logs history items`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val library = LibraryStorage(context)
        val track = SavedTrack(
            id = "hist_1",
            title = "Save Your Tears",
            artist = "The Weeknd"
        )

        library.addToHistory(track)
        assertTrue(library.history.value.any { it.id == "hist_1" })
        library.clearHistory()
        assertTrue(library.history.value.isEmpty())
    }
}
