package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CityStats

@Composable
fun TopBarStats(
    stats: CityStats,
    isAutoSavedRecent: Boolean,
    onSpeedChange: (Int) -> Unit,
    onOpenSaveLoad: () -> Unit,
    onOpenMilestones: () -> Unit,
    onOpenNewCity: () -> Unit,
    onOpenDownloadApk: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("top_bar_stats"),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Row 1: City & Mayor Name, Save Status, Header Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stats.cityName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Subtle auto-save status pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isAutoSavedRecent) Color(0xFF10B981).copy(alpha = 0.18f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isAutoSavedRecent) Color(0xFF10B981) else Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAutoSavedRecent) "Saved" else "Auto-save",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isAutoSavedRecent) Color(0xFF10B981) else Color.Gray
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Milestones Button
                    IconButton(
                        onClick = onOpenMilestones,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("milestones_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Milestones",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Save / Load Menu Button
                    IconButton(
                        onClick = onOpenSaveLoad,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("save_load_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Save and Load Game",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // New City Button
                    IconButton(
                        onClick = onOpenNewCity,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("new_city_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "New City",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Download APK Button
                    IconButton(
                        onClick = onOpenDownloadApk,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("download_apk_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download APK",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 2: Vitals Cards (Funds, Pop, Happiness, Day/Time, Utilities)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Treasury & Net Income
                Column {
                    Text(
                        text = "$${formatFunds(stats.funds)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF10B981)
                    )
                    val netColor = if (stats.netDaily >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                    val netSign = if (stats.netDaily >= 0) "+" else ""
                    Text(
                        text = "$netSign$${stats.netDaily}/d",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = netColor
                    )
                }

                // Population
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "👥 ${stats.population}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Citizens",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Happiness
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val happyEmoji = when {
                        stats.happiness >= 80 -> "😃"
                        stats.happiness >= 60 -> "🙂"
                        else -> "😟"
                    }
                    Text(
                        text = "$happyEmoji ${stats.happiness}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (stats.happiness >= 70) Color(0xFF10B981) else Color(0xFFF59E0B)
                    )
                    Text(
                        text = "Approval",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Power & Water
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Power
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Power",
                            tint = if (stats.isPowerDeficit) Color(0xFFEF4444) else Color(0xFFFACC15),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${stats.powerDemand}/${stats.powerSupply}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (stats.isPowerDeficit) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Water
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Water",
                            tint = if (stats.isWaterDeficit) Color(0xFFEF4444) else Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${stats.waterDemand}/${stats.waterSupply}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (stats.isWaterDeficit) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Time of Day & Speed Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (stats.isNight) Icons.Default.NightlightRound else Icons.Default.WbSunny,
                        contentDescription = "Time",
                        tint = if (stats.isNight) Color(0xFF818CF8) else Color(0xFFF59E0B),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Day ${stats.day}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    // Speed Pill Button (0x, 1x, 2x, 3x)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable {
                                val nextSpeed = when (stats.simulationSpeed) {
                                    0 -> 1
                                    1 -> 2
                                    2 -> 3
                                    else -> 0
                                }
                                onSpeedChange(nextSpeed)
                            }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                            .testTag("speed_toggle_button")
                    ) {
                        Text(
                            text = when (stats.simulationSpeed) {
                                0 -> "⏸ Paused"
                                1 -> "▶ 1x"
                                2 -> "⏩ 2x"
                                else -> "⚡ 3x"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

private fun formatFunds(amount: Long): String {
    return when {
        amount >= 1_000_000 -> String.format("%.1fM", amount / 1_000_000.0)
        amount >= 10_000 -> String.format("%.1fK", amount / 1000.0)
        else -> amount.toString()
    }
}
