package com.example.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.network.spotify.SpotifyRepository
import com.example.network.spotify.SpotifySearchResponse
import com.example.network.spotify.SpotifyTrackItem
import com.example.preferences.LibraryStorage
import com.example.preferences.SavedTrack
import com.example.ui.theme.BurgundyDark
import com.example.ui.theme.BurgundyMedium
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PitchDark
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

enum class SearchFilter(val label: String, val queryType: String) {
    ALL("All", "track,album,artist,playlist"),
    TRACKS("Tracks", "track"),
    ALBUMS("Albums", "album"),
    ARTISTS("Artists", "artist"),
    PLAYLISTS("Playlists", "playlist")
}

@Composable
fun SearchScreen(
    spotifyRepository: SpotifyRepository,
    libraryStorage: LibraryStorage,
    onNavigateToConnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(SearchFilter.ALL) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchResponse by remember { mutableStateOf<SpotifySearchResponse?>(null) }

    val favorites by libraryStorage.favorites.collectAsState()

    fun performSearch() {
        if (searchQuery.isBlank()) return
        focusManager.clearFocus()
        isLoading = true
        errorMessage = null

        scope.launch {
            val result = spotifyRepository.search(searchQuery, selectedFilter.queryType)
            isLoading = false
            if (result.isSuccess) {
                searchResponse = result.getOrNull()
            } else {
                errorMessage = result.exceptionOrNull()?.message ?: "Search failed"
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input_field"),
            placeholder = { Text("Search songs, albums, artists...", color = TextMuted, fontSize = 14.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = CrimsonAccent)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = ""; searchResponse = null }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PitchDark,
                unfocusedContainerColor = PitchDark,
                focusedBorderColor = CrimsonPrimary,
                unfocusedBorderColor = SurfaceCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { performSearch() })
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchFilter.values().forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = {
                        selectedFilter = filter
                        if (searchQuery.isNotBlank()) performSearch()
                    },
                    label = { Text(filter.label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CrimsonPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = SurfaceCard,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilter == filter,
                        borderColor = SurfaceCardBorder,
                        selectedBorderColor = CrimsonGlow
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content Area
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = CrimsonPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Searching Spotify Catalog...", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }

            errorMessage != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PitchDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BurgundyMedium)
                ) {
                    Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = CrimsonAccent, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Spotify Connection Needed", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = errorMessage ?: "Configure Spotify Client ID in settings to search the catalog.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onNavigateToConnect,
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Configure Spotify Connect", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            searchResponse != null -> {
                val tracks = searchResponse?.tracks?.items ?: emptyList()
                val albums = searchResponse?.albums?.items ?: emptyList()
                val artists = searchResponse?.artists?.items ?: emptyList()
                val playlists = searchResponse?.playlists?.items ?: emptyList()

                if (tracks.isEmpty() && albums.isEmpty() && artists.isEmpty() && playlists.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No matching items found.", color = TextMuted, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (tracks.isNotEmpty()) {
                            item {
                                Text(
                                    text = "TRACKS (${tracks.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonAccent,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(tracks) { track ->
                                val isFav = favorites.any { it.id == track.id }
                                TrackResultCard(
                                    track = track,
                                    isFavorite = isFav,
                                    onToggleFavorite = {
                                        val saved = SavedTrack(
                                            id = track.id,
                                            title = track.name,
                                            artist = track.artists.joinToString(", ") { it.name },
                                            album = track.album?.name ?: "",
                                            artUrl = track.album?.images?.firstOrNull()?.url,
                                            spotifyUri = track.uri,
                                            webUrl = track.externalUrls["spotify"]
                                        )
                                        libraryStorage.toggleFavorite(saved)
                                    },
                                    onOpen = {
                                        val saved = SavedTrack(
                                            id = track.id,
                                            title = track.name,
                                            artist = track.artists.joinToString(", ") { it.name },
                                            album = track.album?.name ?: "",
                                            artUrl = track.album?.images?.firstOrNull()?.url,
                                            spotifyUri = track.uri,
                                            webUrl = track.externalUrls["spotify"]
                                        )
                                        libraryStorage.addToHistory(saved)
                                        spotifyRepository.openInSpotify(context, track.uri, track.externalUrls["spotify"])
                                    }
                                )
                            }
                        }

                        if (albums.isNotEmpty() && (selectedFilter == SearchFilter.ALL || selectedFilter == SearchFilter.ALBUMS)) {
                            item {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "ALBUMS (${albums.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonAccent,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(albums) { album ->
                                GenericMediaCard(
                                    title = album.name,
                                    subtitle = "${album.artists.joinToString(", ") { it.name }} • ${album.totalTracks} tracks",
                                    imageUrl = album.images.firstOrNull()?.url,
                                    icon = Icons.Default.Album,
                                    onOpen = {
                                        spotifyRepository.openInSpotify(context, album.uri, album.externalUrls["spotify"])
                                    }
                                )
                            }
                        }

                        if (artists.isNotEmpty() && (selectedFilter == SearchFilter.ALL || selectedFilter == SearchFilter.ARTISTS)) {
                            item {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "ARTISTS (${artists.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonAccent,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(artists) { artist ->
                                GenericMediaCard(
                                    title = artist.name,
                                    subtitle = if (artist.genres.isNotEmpty()) artist.genres.take(3).joinToString(" • ") else "Artist",
                                    imageUrl = artist.images.firstOrNull()?.url,
                                    icon = Icons.Default.Person,
                                    onOpen = {
                                        spotifyRepository.openInSpotify(context, artist.uri, artist.externalUrls["spotify"])
                                    }
                                )
                            }
                        }
                    }
                }
            }

            else -> {
                // Initial prompt / popular suggestions
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = BurgundyMedium, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Search Millions of Tracks on Spotify", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Find artists, albums, or tracks and control them via the floating bubble.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TrackResultCard(
    track: SpotifyTrackItem,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PitchDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imgUrl = track.album?.images?.firstOrNull()?.url
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BurgundyDark),
                contentAlignment = Alignment.Center
            ) {
                if (!imgUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imgUrl,
                        contentDescription = track.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, tint = CrimsonAccent)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = track.artists.joinToString(", ") { it.name } + if (!track.album?.name.isNullOrBlank()) " • " + track.album?.name else "",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) CrimsonPrimary else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onOpen) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "Open in Spotify",
                    tint = CrimsonAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun GenericMediaCard(
    title: String,
    subtitle: String,
    imageUrl: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PitchDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BurgundyDark),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(imageVector = icon, contentDescription = null, tint = CrimsonAccent)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onOpen) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "Open",
                    tint = CrimsonAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
