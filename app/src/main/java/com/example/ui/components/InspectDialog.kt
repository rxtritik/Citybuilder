package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.window.Dialog
import com.example.model.BuildingCategory
import com.example.model.Tile

@Composable
fun InspectDialog(
    tile: Tile,
    currentFunds: Long,
    onUpgrade: () -> Unit,
    onDemolish: () -> Unit,
    onDismiss: () -> Unit
) {
    val b = tile.building

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("inspect_dialog")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header: Title and Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = b?.displayName ?: tile.terrain.displayName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (b != null) "Level ${tile.level} • ${b.category.displayName}" else "Natural Terrain",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (b != null) {
                    // Description
                    Text(
                        text = b.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Utilities & Road Status Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusPill(
                            label = if (tile.isPowered) "Powered" else "No Power",
                            isGood = tile.isPowered || b.powerDemand == 0,
                            icon = Icons.Default.Bolt
                        )
                        StatusPill(
                            label = if (tile.isWatered) "Watered" else "No Water",
                            isGood = tile.isWatered || b.waterDemand == 0,
                            icon = Icons.Default.WaterDrop
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stats Grid
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (b.populationBonus > 0) {
                            StatRow("Citizens Housed", "+${(b.populationBonus * (if (tile.level == 1) 1f else 1.7f)).toInt()}")
                        }
                        if (b.jobsProvided > 0) {
                            StatRow("Jobs Provided", "+${(b.jobsProvided * (if (tile.level == 1) 1f else 1.7f)).toInt()}")
                        }
                        if (b.dailyRevenue > 0) {
                            StatRow("Daily Taxes/Rev", "+$${b.dailyRevenue}/day")
                        }
                        if (b.dailyUpkeep > 0) {
                            StatRow("Daily Maintenance", "-$${b.dailyUpkeep}/day")
                        }
                        if (b.powerSupply > 0) {
                            StatRow("Clean Electricity", "+${b.powerSupply} MW")
                        }
                        if (b.waterSupply > 0) {
                            StatRow("Fresh Water", "+${b.waterSupply} kGal")
                        }
                        if (tile.pollution > 0) {
                            StatRow("Local Pollution", "${tile.pollution} ppm", isWarning = true)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons: Upgrade & Demolish
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (tile.level < tile.maxLevel) {
                            val upgradeCost = tile.upgradeCost
                            val canAfford = currentFunds >= upgradeCost
                            Button(
                                onClick = onUpgrade,
                                enabled = canAfford,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("upgrade_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Upgrade ($$upgradeCost)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = onDemolish,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("demolish_tile_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFEF4444))
                            Spacer(modifier = Modifier.width(4.dp))
                            val refund = (b.cost * 0.4f).toInt()
                            Text("Demolish (+$refund)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        }
                    }
                } else {
                    // Empty or natural terrain
                    Text(
                        text = "This is undeveloped ${tile.terrain.displayName.lowercase()}. Select a building from the toolbar to develop this plot.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPill(label: String, isGood: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val bgColor = if (isGood) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
    val contentColor = if (isGood) Color(0xFF10B981) else Color(0xFFEF4444)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = contentColor)
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, isWarning: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isWarning) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
        )
    }
}
