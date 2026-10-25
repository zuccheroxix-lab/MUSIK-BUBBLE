package com.example.network.spotify

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class SpotifyRepository(context: Context) {

    companion object {
        private const val TAG = "SpotifyRepository"
        private const val PREFS_NAME = "spotify_settings_prefs"
        private const val KEY_CLIENT_ID = "spotify_client_id"
        private const val KEY_CLIENT_SECRET = "spotify_client_secret"

        // Default public demo client ID placeholder (can be customized by user in app settings)
        private const val DEFAULT_DEMO_CLIENT_ID = ""
        private const val DEFAULT_DEMO_CLIENT_SECRET = ""
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val authApi: SpotifyAuthApi = Retrofit.Builder()
        .baseUrl("https://accounts.spotify.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(SpotifyAuthApi::class.java)

    private val catalogApi: SpotifyCatalogApi = Retrofit.Builder()
        .baseUrl("https://api.spotify.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(SpotifyCatalogApi::class.java)

    private var cachedToken: String? = null
    private var tokenExpiryEpochMs: Long = 0L

    private val _connectionState = MutableStateFlow<SpotifyConnectionStatus>(SpotifyConnectionStatus.NotConfigured)
    val connectionState: StateFlow<SpotifyConnectionStatus> = _connectionState.asStateFlow()

    init {
        checkInitialStatus()
    }

    var clientId: String
        get() = prefs.getString(KEY_CLIENT_ID, DEFAULT_DEMO_CLIENT_ID) ?: ""
        set(value) {
            prefs.edit().putString(KEY_CLIENT_ID, value.trim()).apply()
            cachedToken = null
            checkInitialStatus()
        }

    var clientSecret: String
        get() = prefs.getString(KEY_CLIENT_SECRET, DEFAULT_DEMO_CLIENT_SECRET) ?: ""
        set(value) {
            prefs.edit().putString(KEY_CLIENT_SECRET, value.trim()).apply()
            cachedToken = null
            checkInitialStatus()
        }

    private fun checkInitialStatus() {
        if (clientId.isBlank() || clientSecret.isBlank()) {
            _connectionState.value = SpotifyConnectionStatus.NotConfigured
        } else {
            _connectionState.value = SpotifyConnectionStatus.Configured
        }
    }

    suspend fun authenticate(): Result<String> = withContext(Dispatchers.IO) {
        val cid = clientId
        val csec = clientSecret

        if (cid.isBlank() || csec.isBlank()) {
            _connectionState.value = SpotifyConnectionStatus.NotConfigured
            return@withContext Result.failure(
                IllegalStateException("Spotify Client ID and Secret are not configured. Please enter them in the Spotify Connect tab.")
            )
        }

        val now = System.currentTimeMillis()
        if (cachedToken != null && now < tokenExpiryEpochMs - 60_000) {
            return@withContext Result.success(cachedToken!!)
        }

        try {
            _connectionState.value = SpotifyConnectionStatus.Connecting
            val authValue = "Basic " + Base64.encodeToString("$cid:$csec".toByteArray(), Base64.NO_WRAP)
            val response = authApi.getAccessToken(authValue)
            cachedToken = response.accessToken
            tokenExpiryEpochMs = now + (response.expiresIn * 1000L)
            _connectionState.value = SpotifyConnectionStatus.Connected
            Result.success(response.accessToken)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to authenticate with Spotify API", e)
            _connectionState.value = SpotifyConnectionStatus.Error(e.message ?: "Authentication failed")
            Result.failure(e)
        }
    }

    suspend fun search(query: String, type: String = "track,album,artist,playlist"): Result<SpotifySearchResponse> =
        withContext(Dispatchers.IO) {
            if (query.isBlank()) {
                return@withContext Result.success(
                    SpotifySearchResponse(null, null, null, null)
                )
            }

            val tokenResult = authenticate()
            if (tokenResult.isFailure) {
                return@withContext Result.failure(tokenResult.exceptionOrNull()!!)
            }

            val token = tokenResult.getOrThrow()
            try {
                val bearer = "Bearer $token"
                val response = catalogApi.search(bearer, query, type)
                Result.success(response)
            } catch (e: Exception) {
                Log.e(TAG, "Search query failed for: $query", e)
                Result.failure(e)
            }
        }

    fun openInSpotify(context: Context, spotifyUri: String?, webUrl: String?): Boolean {
        // Try to open using native Spotify URI scheme (e.g. spotify:track:...)
        if (!spotifyUri.isNullOrBlank()) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(spotifyUri)).apply {
                    setPackage("com.spotify.music")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return true
            } catch (_: Exception) {
                // If Spotify app is not installed, open fallback web URL
            }
        }

        if (!webUrl.isNullOrBlank()) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return true
            } catch (e: Exception) {
                Log.w(TAG, "Could not open Spotify web URL", e)
            }
        }
        return false
    }
}

sealed class SpotifyConnectionStatus {
    object NotConfigured : SpotifyConnectionStatus()
    object Configured : SpotifyConnectionStatus()
    object Connecting : SpotifyConnectionStatus()
    object Connected : SpotifyConnectionStatus()
    data class Error(val message: String) : SpotifyConnectionStatus()
}
