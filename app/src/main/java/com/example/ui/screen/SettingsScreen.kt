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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.automirrored.filled.ViewSidebar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.preferences.BubblePreferences
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

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { BubblePreferences(context) }

    var bubbleSize by remember { mutableIntStateOf(prefs.bubbleSizeDp) }
    var bubbleAlpha by remember { mutableFloatStateOf(prefs.bubbleAlpha) }
    var panelAlpha by remember { mutableFloatStateOf(prefs.panelAlpha) }
    var autoSnap by remember { mutableStateOf(prefs.autoSnap) }
    var vibrate by remember { mutableStateOf(prefs.vibrateOnTap) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "OVERLAY SETTINGS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = TextPrimary
        )
        Text(
            text = "Customize the floating bubble size, opacity, and touch physics.",
            fontSize = 13.sp,
            color = TextSecondary
        )

        // Live Visual Preview Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PitchDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BurgundyMedium)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LIVE BUBBLE PREVIEW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = CrimsonAccent
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(bubbleSize.dp)
                        .alpha(bubbleAlpha)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(BurgundyMedium, BurgundyDark)
                            )
                        )
                        .border(2.5.dp, CrimsonPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Preview",
                        tint = CrimsonAccent,
                        modifier = Modifier.size((bubbleSize * 0.45f).dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Size: ${bubbleSize}dp  •  Opacity: ${(bubbleAlpha * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Size & Opacity Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PitchDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Bubble Size Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Bubble Diameter", fontSize = 14.sp, color = TextPrimary)
                    Text(text = "${bubbleSize} dp", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CrimsonGlow)
                }
                Slider(
                    value = bubbleSize.toFloat(),
                    onValueChange = {
                        bubbleSize = it.toInt()
                        prefs.bubbleSizeDp = bubbleSize
                    },
                    valueRange = 48f..76f,
                    steps = 6,
                    colors = SliderDefaults.colors(
                        thumbColor = CrimsonPrimary,
                        activeTrackColor = CrimsonPrimary,
                        inactiveTrackColor = BurgundyDark
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Bubble Alpha Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Bubble Opacity", fontSize = 14.sp, color = TextPrimary)
                    Text(text = "${(bubbleAlpha * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CrimsonGlow)
                }
                Slider(
                    value = bubbleAlpha,
                    onValueChange = {
                        bubbleAlpha = it
                        prefs.bubbleAlpha = bubbleAlpha
                    },
                    valueRange = 0.4f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = CrimsonPrimary,
                        activeTrackColor = CrimsonPrimary,
                        inactiveTrackColor = BurgundyDark
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Panel Alpha Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Mini Player Panel Opacity", fontSize = 14.sp, color = TextPrimary)
                    Text(text = "${(panelAlpha * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CrimsonGlow)
                }
                Slider(
                    value = panelAlpha,
                    onValueChange = {
                        panelAlpha = it
                        prefs.panelAlpha = panelAlpha
                    },
                    valueRange = 0.4f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = CrimsonPrimary,
                        activeTrackColor = CrimsonPrimary,
                        inactiveTrackColor = BurgundyDark
                    )
                )
            }
        }

        // Toggles Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PitchDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Auto-snap
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ViewSidebar,
                            contentDescription = null,
                            tint = CrimsonAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Auto-Snap to Screen Edge", fontSize = 14.sp, color = TextPrimary)
                            Text(
                                text = "Docks bubble magnetically to nearest edge on release",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                    Switch(
                        checked = autoSnap,
                        onCheckedChange = {
                            autoSnap = it
                            prefs.autoSnap = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CrimsonPrimary,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SurfaceCard
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Vibrate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = CrimsonAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Haptic Tactile Feedback", fontSize = 14.sp, color = TextPrimary)
                            Text(
                                text = "Subtle vibration on tapping bubble and controls",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                    Switch(
                        checked = vibrate,
                        onCheckedChange = {
                            vibrate = it
                            prefs.vibrateOnTap = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CrimsonPrimary,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SurfaceCard
                        )
                    )
                }
            }
        }

        // Reset Defaults Button
        OutlinedButton(
            onClick = {
                bubbleSize = BubblePreferences.DEFAULT_BUBBLE_SIZE
                bubbleAlpha = BubblePreferences.DEFAULT_BUBBLE_ALPHA
                panelAlpha = BubblePreferences.DEFAULT_PANEL_ALPHA
                autoSnap = true
                vibrate = true

                prefs.bubbleSizeDp = bubbleSize
                prefs.bubbleAlpha = bubbleAlpha
                prefs.panelAlpha = panelAlpha
                prefs.autoSnap = true
                prefs.vibrateOnTap = true
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
            border = androidx.compose.foundation.BorderStroke(1.dp, BurgundyDark)
        ) {
            Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Restore Default Settings", fontSize = 13.sp)
        }
    }
}
