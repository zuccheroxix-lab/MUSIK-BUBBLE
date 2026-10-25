package com.example.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.spotify.SpotifyConnectionStatus
import com.example.network.spotify.SpotifyRepository
import com.example.ui.theme.BurgundyDark
import com.example.ui.theme.BurgundyMedium
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PitchDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.launch

@Composable
fun SpotifyConnectScreen(
    spotifyRepository: SpotifyRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var inputClientId by remember { mutableStateOf(spotifyRepository.clientId) }
    var inputClientSecret by remember { mutableStateOf(spotifyRepository.clientSecret) }
    var isSecretVisible by remember { mutableStateOf(false) }

    val connectionState by spotifyRepository.connectionState.collectAsState()
    var isTestingConnection by remember { mutableStateOf(false) }
    var testFeedback by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(BurgundyDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Radio, contentDescription = null, tint = CrimsonAccent, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "SPOTIFY CONNECT",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Official Web API catalog and Spotify playback integration.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Connection Status Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PitchDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BurgundyMedium)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (statusIcon, statusColor, statusLabel) = when (connectionState) {
                    is SpotifyConnectionStatus.Connected -> Triple(Icons.Default.CheckCircle, SuccessGreen, "Connected & Operational")
                    is SpotifyConnectionStatus.Connecting -> Triple(Icons.Default.Sync, WarningAmber, "Connecting...")
                    is SpotifyConnectionStatus.Configured -> Triple(Icons.Default.Security, CrimsonAccent, "Configured (Ready to Test)")
                    is SpotifyConnectionStatus.Error -> Triple(Icons.Default.Warning, ErrorRed, "Connection Failed")
                    is SpotifyConnectionStatus.NotConfigured -> Triple(Icons.Default.Info, WarningAmber, "Credentials Not Configured")
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(22.dp))
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "API STATUS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CrimsonGlow, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = statusLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    if (connectionState is SpotifyConnectionStatus.Error) {
                        Text(
                            text = (connectionState as SpotifyConnectionStatus.Error).message,
                            fontSize = 11.sp,
                            color = ErrorRed
                        )
                    }
                }
            }
        }

        // Credentials Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PitchDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "SPOTIFY DEVELOPER CREDENTIALS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonAccent,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enter your Client ID and Client Secret to enable catalog search and metadata fetching.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Client ID
                OutlinedTextField(
                    value = inputClientId,
                    onValueChange = { inputClientId = it },
                    label = { Text("Client ID", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("spotify_client_id_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard,
                        focusedBorderColor = CrimsonPrimary,
                        unfocusedBorderColor = SurfaceCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Client Secret
                OutlinedTextField(
                    value = inputClientSecret,
                    onValueChange = { inputClientSecret = it },
                    label = { Text("Client Secret", fontSize = 12.sp) },
                    singleLine = true,
                    visualTransformation = if (isSecretVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { isSecretVisible = !isSecretVisible }) {
                            Icon(
                                imageVector = if (isSecretVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Secret",
                                tint = TextMuted
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("spotify_client_secret_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard,
                        focusedBorderColor = CrimsonPrimary,
                        unfocusedBorderColor = SurfaceCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            spotifyRepository.clientId = inputClientId
                            spotifyRepository.clientSecret = inputClientSecret

                            isTestingConnection = true
                            testFeedback = null
                            scope.launch {
                                val result = spotifyRepository.authenticate()
                                isTestingConnection = false
                                testFeedback = if (result.isSuccess) {
                                    "Credentials verified successfully! Spotify Catalog search is now active."
                                } else {
                                    "Error: ${result.exceptionOrNull()?.message}"
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_spotify_credentials_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Save & Test Connection", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                testFeedback?.let { feedback ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = feedback,
                        fontSize = 12.sp,
                        color = if (feedback.startsWith("Credentials verified")) SuccessGreen else ErrorRed
                    )
                }
            }
        }

        // How-to guide card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = BurgundyDark.copy(alpha = 0.35f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, BurgundyDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📖 Quick Spotify Developer Guide",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonAccent
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "1. Open developer.spotify.com/dashboard in your browser.\n" +
                            "2. Log in with your free Spotify account and click 'Create an app'.\n" +
                            "3. Name it 'Music Bubble' and set App type to 'Mobile App'.\n" +
                            "4. Go to Settings, copy your Client ID & Client Secret, and paste them above.\n" +
                            "5. Tap 'Save & Test Connection' to unlock real online search!",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://developer.spotify.com/dashboard")).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonAccent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonPrimary)
                ) {
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Spotify Developer Dashboard", fontSize = 12.sp)
                }
            }
        }

        // Direct Launch Spotify App
        OutlinedButton(
            onClick = {
                val launchIntent = context.packageManager.getLaunchIntentForPackage("com.spotify.music")
                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                } else {
                    val playStore = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.spotify.music")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    try {
                        context.startActivity(playStore)
                    } catch (_: Exception) {}
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
        ) {
            Icon(imageVector = Icons.Default.Radio, contentDescription = null, modifier = Modifier.size(16.dp), tint = SuccessGreen)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Launch Spotify App on Device", fontSize = 13.sp)
        }
    }
}
