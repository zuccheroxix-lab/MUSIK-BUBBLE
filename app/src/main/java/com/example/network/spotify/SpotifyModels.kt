package com.example.network.spotify

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SpotifyTokenResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String,
    @Json(name = "expires_in") val expiresIn: Int
)

@JsonClass(generateAdapter = true)
data class SpotifySearchResponse(
    val tracks: SpotifyTrackPaging?,
    val albums: SpotifyAlbumPaging?,
    val artists: SpotifyArtistPaging?,
    val playlists: SpotifyPlaylistPaging?
)

@JsonClass(generateAdapter = true)
data class SpotifyTrackPaging(
    val items: List<SpotifyTrackItem> = emptyList(),
    val total: Int = 0
)

@JsonClass(generateAdapter = true)
data class SpotifyAlbumPaging(
    val items: List<SpotifyAlbumItem> = emptyList(),
    val total: Int = 0
)

@JsonClass(generateAdapter = true)
data class SpotifyArtistPaging(
    val items: List<SpotifyArtistItem> = emptyList(),
    val total: Int = 0
)

@JsonClass(generateAdapter = true)
data class SpotifyPlaylistPaging(
    val items: List<SpotifyPlaylistItem> = emptyList(),
    val total: Int = 0
)

@JsonClass(generateAdapter = true)
data class SpotifyTrackItem(
    val id: String,
    val name: String,
    val artists: List<SpotifyArtistSimple> = emptyList(),
    val album: SpotifyAlbumSimple? = null,
    @Json(name = "duration_ms") val durationMs: Long = 0L,
    @Json(name = "preview_url") val previewUrl: String? = null,
    val uri: String? = null,
    @Json(name = "external_urls") val externalUrls: Map<String, String> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class SpotifyAlbumItem(
    val id: String,
    val name: String,
    val artists: List<SpotifyArtistSimple> = emptyList(),
    val images: List<SpotifyImage> = emptyList(),
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "total_tracks") val totalTracks: Int = 0,
    val uri: String? = null,
    @Json(name = "external_urls") val externalUrls: Map<String, String> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class SpotifyArtistItem(
    val id: String,
    val name: String,
    val genres: List<String> = emptyList(),
    val images: List<SpotifyImage> = emptyList(),
    val uri: String? = null,
    @Json(name = "external_urls") val externalUrls: Map<String, String> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class SpotifyPlaylistItem(
    val id: String,
    val name: String,
    val description: String? = null,
    val images: List<SpotifyImage> = emptyList(),
    val uri: String? = null,
    @Json(name = "external_urls") val externalUrls: Map<String, String> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class SpotifyArtistSimple(
    val id: String? = null,
    val name: String = ""
)

@JsonClass(generateAdapter = true)
data class SpotifyAlbumSimple(
    val id: String? = null,
    val name: String = "",
    val images: List<SpotifyImage> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SpotifyImage(
    val url: String,
    val height: Int? = null,
    val width: Int? = null
)
