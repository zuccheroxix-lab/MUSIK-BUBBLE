package com.example.ui.screen

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.ui.theme.WarningAmber
import com.example.util.PermissionHelper

@Composable
fun PermissionsScreen(
    hasOverlayPerm: Boolean,
    isNotificationConnected: Boolean,
    hasPostNotifPerm: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        onRefresh()
    }

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
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = CrimsonAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "ANDROID PERMISSIONS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Required to render the overlay and read live playback metadata.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // 1. SYSTEM_ALERT_WINDOW
        PermissionItemCard(
            title = "Display Over Other Apps",
            permissionName = "SYSTEM_ALERT_WINDOW",
            description = "Allows Music Bubble to draw the floating mini player above games, social media, and other running applications.",
            isGranted = hasOverlayPerm,
            buttonText = if (hasOverlayPerm) "Overlay Access Granted" else "Grant Overlay Permission",
            icon = Icons.Default.Layers,
            onClick = {
                PermissionHelper.openOverlaySettings(context)
            }
        )

        // 2. BIND_NOTIFICATION_LISTENER_SERVICE / MediaSession
        PermissionItemCard(
            title = "Notification & Media Session Access",
            permissionName = "BIND_NOTIFICATION_LISTENER_SERVICE",
            description = "Enables system MediaSessionManager to detect current song titles, artist names, album art, and forward play/pause commands to Spotify or YouTube Music.",
            isGranted = isNotificationConnected,
            buttonText = if (isNotificationConnected) "Media Access Connected" else "Enable Media Access in Settings",
            icon = Icons.Default.Radio,
            onClick = {
                PermissionHelper.openNotificationListenerSettings(context)
            }
        )

        // 3. POST_NOTIFICATIONS (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionItemCard(
                title = "Foreground Notification",
                permissionName = "POST_NOTIFICATIONS",
                description = "Required on Android 13+ to display the ongoing service controller notification that keeps the floating overlay alive in memory.",
                isGranted = hasPostNotifPerm,
                buttonText = if (hasPostNotifPerm) "Notifications Allowed" else "Allow Notifications",
                icon = Icons.Default.Notifications,
                onClick = {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            )
        }

        // 4. Battery Optimization
        val isIgnoringBattery = PermissionHelper.isIgnoringBatteryOptimizations(context)
        PermissionItemCard(
            title = "Unrestricted Battery Usage",
            permissionName = "REQUEST_IGNORE_BATTERY_OPTIMIZATIONS",
            description = "Prevents Android OEM power managers (MIUI, OneUI, ColorOS) from killing the floating overlay service when switching between heavy apps.",
            isGranted = isIgnoringBattery,
            buttonText = if (isIgnoringBattery) "Battery Unrestricted" else "Configure Battery Settings",
            icon = Icons.Default.BatteryAlert,
            onClick = {
                PermissionHelper.openBatteryOptimizationSettings(context)
            }
        )
    }
}

@Composable
private fun PermissionItemCard(
    title: String,
    permissionName: String,
    description: String,
    isGranted: Boolean,
    buttonText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PitchDark),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isGranted) SurfaceCardBorder else BurgundyMedium
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isGranted) BurgundyDark else SurfaceCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isGranted) SuccessGreen else CrimsonAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = permissionName,
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isGranted) SuccessGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isGranted) "ACTIVE" else "REQUIRED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGranted) SuccessGreen else WarningAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onClick,
                enabled = !isGranted,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGranted) BurgundyDark else CrimsonPrimary,
                    disabledContainerColor = BurgundyDark.copy(alpha = 0.6f)
                )
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isGranted) SuccessGreen else Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buttonText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isGranted) SuccessGreen else Color.White
                )
            }
        }
    }
}
