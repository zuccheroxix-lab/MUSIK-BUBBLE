package com.example.preferences

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@JsonClass(generateAdapter = true)
data class SavedTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val artUrl: String? = null,
    val spotifyUri: String? = null,
    val webUrl: String? = null,
    val localUriString: String? = null,
    val addedTimestamp: Long = System.currentTimeMillis()
)

class LibraryStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("music_bubble_library", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, SavedTrack::class.java)
    private val adapter = moshi.adapter<List<SavedTrack>>(listType)

    private val _favorites = MutableStateFlow<List<SavedTrack>>(emptyList())
    val favorites: StateFlow<List<SavedTrack>> = _favorites.asStateFlow()

    private val _history = MutableStateFlow<List<SavedTrack>>(emptyList())
    val history: StateFlow<List<SavedTrack>> = _history.asStateFlow()

    companion object {
        private const val KEY_FAVORITES = "lib_favorites_json"
        private const val KEY_HISTORY = "lib_history_json"
    }

    init {
        loadData()
    }

    private fun loadData() {
        val favJson = prefs.getString(KEY_FAVORITES, null)
        if (!favJson.isNullOrBlank()) {
            try {
                _favorites.value = adapter.fromJson(favJson) ?: emptyList()
            } catch (_: Exception) {}
        }

        val histJson = prefs.getString(KEY_HISTORY, null)
        if (!histJson.isNullOrBlank()) {
            try {
                _history.value = adapter.fromJson(histJson) ?: emptyList()
            } catch (_: Exception) {}
        }
    }

    fun isFavorite(trackId: String): Boolean {
        return _favorites.value.any { it.id == trackId }
    }

    fun toggleFavorite(track: SavedTrack) {
        val current = _favorites.value.toMutableList()
        val index = current.indexOfFirst { it.id == track.id }
        if (index >= 0) {
            current.removeAt(index)
        } else {
            current.add(0, track)
        }
        _favorites.value = current
        saveFavorites(current)
    }

    fun addToHistory(track: SavedTrack) {
        val current = _history.value.toMutableList()
        current.removeAll { it.id == track.id }
        current.add(0, track)
        val limited = current.take(50)
        _history.value = limited
        saveHistory(limited)
    }

    fun clearHistory() {
        _history.value = emptyList()
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun saveFavorites(list: List<SavedTrack>) {
        try {
            val json = adapter.toJson(list)
            prefs.edit().putString(KEY_FAVORITES, json).apply()
        } catch (_: Exception) {}
    }

    private fun saveHistory(list: List<SavedTrack>) {
        try {
            val json = adapter.toJson(list)
            prefs.edit().putString(KEY_HISTORY, json).apply()
        } catch (_: Exception) {}
    }
}
