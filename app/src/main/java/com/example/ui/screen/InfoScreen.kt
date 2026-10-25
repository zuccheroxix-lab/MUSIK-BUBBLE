package com.example.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.theme.BurgundyDark
import com.example.ui.theme.BurgundyMedium
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PitchDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun InfoScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

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
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BurgundyDark)
                    .border(1.dp, CrimsonPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = CrimsonAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "APPLICATION INFO",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Architecture details, supported music apps, and troubleshooting.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // App Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PitchDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "SPECIFICATIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = CrimsonAccent
                )
                Spacer(modifier = Modifier.height(10.dp))

                SpecItem("Application Name", "Music Bubble")
                SpecItem("Application ID", BuildConfig.APPLICATION_ID)
                SpecItem("Build Type", BuildConfig.BUILD_TYPE.uppercase())
                SpecItem("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                SpecItem("Minimum SDK", "Android 7.0 (API 24)")
                SpecItem("Target SDK", "Android 15 / 16 (API 36)")
            }
        }

        // Supported Music Players
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PitchDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BurgundyMedium)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Album,
                        contentDescription = null,
                        tint = CrimsonAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SUPPORTED MEDIA PLAYERS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Music Bubble uses standard Android MediaSession APIs and works out-of-the-box with any modern Android audio app:",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                val players = listOf(
                    "Spotify", "YouTube Music", "Apple Music", "Tidal", "SoundCloud",
                    "Deezer", "VLC for Android", "PowerAmp", "Musicolet", "BlackPlayer",
                    "Samsung Music", "Mi Music", "Pocket Casts", "Audible"
                )

                Text(
                    text = players.joinToString("  •  "),
                    fontSize = 12.sp,
                    color = CrimsonGlow,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Manufacturer Troubleshooting (Xiaomi, Samsung, Oppo, Vivo)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PitchDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = CrimsonAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MANUFACTURER TROUBLESHOOTING",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                TroubleshootItem(
                    brand = "Xiaomi / Redmi / POCO (HyperOS / MIUI)",
                    steps = "Open Security app → Manage apps → Music Bubble → Enable 'Autostart' and grant 'Display pop-up windows while running in the background'."
                )
                Spacer(modifier = Modifier.height(8.dp))

                TroubleshootItem(
                    brand = "Samsung (One UI)",
                    steps = "Settings → Battery and device care → Battery → Background usage limits → Add Music Bubble to 'Never sleeping apps'."
                )
                Spacer(modifier = Modifier.height(8.dp))

                TroubleshootItem(
                    brand = "Oppo / Realme / Vivo",
                    steps = "Settings → Battery → App battery management → Enable 'Allow background activity' and 'Allow auto-launch'."
                )
            }
        }

        // Privacy Guarantee Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = BurgundyDark.copy(alpha = 0.35f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, BurgundyDark)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "100% Offline & Private",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Music Bubble does not collect, record, or transmit any audio, titles, or user telemetry. All MediaSession actions operate purely on-device.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

@Composable
private fun TroubleshootItem(brand: String, steps: String) {
    Column {
        Text(text = brand, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CrimsonGlow)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = steps, fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
    }
}
