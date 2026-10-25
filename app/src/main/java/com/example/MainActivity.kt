package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.network.spotify.SpotifyRepository
import com.example.player.LocalAudioPlayerManager
import com.example.preferences.LibraryStorage
import com.example.service.MediaSessionHub
import com.example.ui.screen.HomeScreen
import com.example.ui.screen.InfoScreen
import com.example.ui.screen.LibraryScreen
import com.example.ui.screen.LocalPlayerScreen
import com.example.ui.screen.PermissionsScreen
import com.example.ui.screen.SearchScreen
import com.example.ui.screen.SettingsScreen
import com.example.ui.screen.SpotifyConnectScreen
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PitchDark
import com.example.ui.theme.TextMuted
import com.example.util.PermissionHelper

enum class AppTab(val title: String) {
    HOME("Home"),
    SEARCH("Search"),
    LOCAL("Local"),
    LIBRARY("Library"),
    OVERLAY("Overlay"),
    CONNECT("Spotify"),
    PERMISSIONS("Perms"),
    INFO("Info")
}

class MainActivity : ComponentActivity() {

    private var refreshTrigger by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        LocalAudioPlayerManager.init(applicationContext)

        setContent {
            MyApplicationTheme {
                val spotifyRepo = remember { SpotifyRepository(applicationContext) }
                val libraryStorage = remember { LibraryStorage(applicationContext) }

                MainContent(
                    spotifyRepository = spotifyRepo,
                    libraryStorage = libraryStorage,
                    refreshKey = refreshTrigger,
                    onRefreshPermissions = { refreshTrigger++ }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshTrigger++
    }
}

@Composable
fun MainContent(
    spotifyRepository: SpotifyRepository,
    libraryStorage: LibraryStorage,
    refreshKey: Int,
    onRefreshPermissions: () -> Unit
) {
    val context = LocalContext.current

    var currentTab by remember { mutableStateOf(AppTab.HOME) }

    var hasOverlayPerm by remember { mutableStateOf(false) }
    var isNotificationConnected by remember { mutableStateOf(false) }
    var hasPostNotifPerm by remember { mutableStateOf(false) }

    LaunchedEffect(refreshKey) {
        hasOverlayPerm = PermissionHelper.hasOverlayPermission(context)
        isNotificationConnected = PermissionHelper.hasNotificationListenerPermission(context)
        hasPostNotifPerm = PermissionHelper.hasPostNotificationsPermission(context)
    }

    val mediaState by MediaSessionHub.mediaState.collectAsStateWithLifecycle()
    val isOverlayActive by MediaSessionHub.isOverlayActive.collectAsStateWithLifecycle()
    val isListenerConnectedState by MediaSessionHub.isNotificationServiceConnected.collectAsStateWithLifecycle()

    val actualNotificationConnected = isNotificationConnected || isListenerConnectedState

    // Handle back button on sub-screens
    BackHandler(enabled = currentTab != AppTab.HOME) {
        currentTab = AppTab.HOME
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack),
        bottomBar = {
            NavigationBar(
                containerColor = PitchDark,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                // Home
                NavigationBarItem(
                    selected = currentTab == AppTab.HOME,
                    onClick = { currentTab = AppTab.HOME },
                    icon = {
                        Icon(imageVector = Icons.Default.PlayCircle, contentDescription = "Home")
                    },
                    label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CrimsonGlow,
                        indicatorColor = CrimsonPrimary,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                // Search
                NavigationBarItem(
                    selected = currentTab == AppTab.SEARCH,
                    onClick = { currentTab = AppTab.SEARCH },
                    icon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    },
                    label = { Text("Search", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CrimsonGlow,
                        indicatorColor = CrimsonPrimary,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                // Local Player
                NavigationBarItem(
                    selected = currentTab == AppTab.LOCAL,
                    onClick = { currentTab = AppTab.LOCAL },
                    icon = {
                        Icon(imageVector = Icons.Default.FolderOpen, contentDescription = "Local")
                    },
                    label = { Text("Local", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CrimsonGlow,
                        indicatorColor = CrimsonPrimary,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                // Library
                NavigationBarItem(
                    selected = currentTab == AppTab.LIBRARY,
                    onClick = { currentTab = AppTab.LIBRARY },
                    icon = {
                        Icon(imageVector = Icons.Default.Favorite, contentDescription = "Library")
                    },
                    label = { Text("Library", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CrimsonGlow,
                        indicatorColor = CrimsonPrimary,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                // Overlay Settings
                NavigationBarItem(
                    selected = currentTab == AppTab.OVERLAY,
                    onClick = { currentTab = AppTab.OVERLAY },
                    icon = {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = "Overlay")
                    },
                    label = { Text("Overlay", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CrimsonGlow,
                        indicatorColor = CrimsonPrimary,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                // Spotify Connect & More
                NavigationBarItem(
                    selected = currentTab == AppTab.CONNECT || currentTab == AppTab.PERMISSIONS || currentTab == AppTab.INFO,
                    onClick = { currentTab = AppTab.CONNECT },
                    icon = {
                        Icon(imageVector = Icons.Default.Radio, contentDescription = "Connect")
                    },
                    label = { Text("Connect", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CrimsonGlow,
                        indicatorColor = CrimsonPrimary,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "tabTransition",
            modifier = Modifier.padding(innerPadding)
        ) { tab ->
            when (tab) {
                AppTab.HOME -> HomeScreen(
                    mediaState = mediaState,
                    isOverlayActive = isOverlayActive,
                    isNotificationConnected = actualNotificationConnected,
                    hasOverlayPerm = hasOverlayPerm,
                    onNavigateToPermissions = { currentTab = AppTab.PERMISSIONS },
                    onNavigateToSearch = { currentTab = AppTab.SEARCH },
                    onNavigateToLocal = { currentTab = AppTab.LOCAL }
                )
                AppTab.SEARCH -> SearchScreen(
                    spotifyRepository = spotifyRepository,
                    libraryStorage = libraryStorage,
                    onNavigateToConnect = { currentTab = AppTab.CONNECT }
                )
                AppTab.LOCAL -> LocalPlayerScreen()
                AppTab.LIBRARY -> LibraryScreen(
                    libraryStorage = libraryStorage,
                    spotifyRepository = spotifyRepository
                )
                AppTab.OVERLAY -> SettingsScreen()
                AppTab.CONNECT -> SpotifyConnectScreen(
                    spotifyRepository = spotifyRepository
                )
                AppTab.PERMISSIONS -> PermissionsScreen(
                    hasOverlayPerm = hasOverlayPerm,
                    isNotificationConnected = actualNotificationConnected,
                    hasPostNotifPerm = hasPostNotifPerm,
                    onRefresh = onRefreshPermissions
                )
                AppTab.INFO -> InfoScreen()
            }
        }
    }
}
