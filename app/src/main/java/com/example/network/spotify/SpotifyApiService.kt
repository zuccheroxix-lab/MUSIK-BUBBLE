package com.example.network.spotify

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SpotifyAuthApi {
    @FormUrlEncoded
    @POST("api/token")
    suspend fun getAccessToken(
        @Header("Authorization") basicAuth: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): SpotifyTokenResponse
}

interface SpotifyCatalogApi {
    @GET("v1/search")
    suspend fun search(
        @Header("Authorization") authHeader: String,
        @Query("q") query: String,
        @Query("type") type: String = "track,album,artist,playlist",
        @Query("limit") limit: Int = 20
    ): SpotifySearchResponse

    @GET("v1/tracks/{id}")
    suspend fun getTrack(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String
    ): SpotifyTrackItem

    @GET("v1/albums/{id}")
    suspend fun getAlbum(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String
    ): SpotifyAlbumItem
}
