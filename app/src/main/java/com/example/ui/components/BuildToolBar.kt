package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BuildingCategory
import com.example.model.BuildingType

@Composable
fun BuildToolBar(
    currentFunds: Long,
    population: Int,
    selectedBuilding: BuildingType?,
    isBulldozeMode: Boolean,
    onSelectBuilding: (BuildingType?) -> Unit,
    onToggleBulldoze: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeCategory by remember { mutableStateOf(BuildingCategory.ROADS) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("build_toolbar"),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        shadowElevation = 10.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 10.dp)
        ) {
            // Active Selection Preview Bar (if building or bulldoze is active)
            AnimatedVisibility(visible = selectedBuilding != null || isBulldozeMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isBulldozeMode) Color(0xFFEF4444).copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isBulldozeMode) Icons.Default.Delete else Icons.Default.Build,
                            contentDescription = null,
                            tint = if (isBulldozeMode) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBulldozeMode) "Bulldozer Active - Tap tile to demolish"
                            else "Ready: ${selectedBuilding?.displayName} ($${selectedBuilding?.cost})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBulldozeMode) Color(0xFFEF4444) else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    IconButton(
                        onClick = {
                            if (isBulldozeMode) onToggleBulldoze()
                            else onSelectBuilding(null)
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Tool",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Category Bar + Bulldozer Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bulldoze Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isBulldozeMode) Color(0xFFEF4444) else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onToggleBulldoze() }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                        .testTag("bulldoze_mode_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Bulldozer",
                            tint = if (isBulldozeMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Demolish",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBulldozeMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Scrollable category tabs
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BuildingCategory.entries.forEach { category ->
                        val isSelected = activeCategory == category && !isBulldozeMode
                        val icon = getCategoryIcon(category)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    activeCategory = category
                                    if (isBulldozeMode) onToggleBulldoze()
                                }
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                                .testTag("category_tab_${category.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = category.displayName,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = category.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Buildings in selected category (horizontal card list)
            val categoryBuildings = BuildingType.entries.filter { it.category == activeCategory }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                categoryBuildings.forEach { building ->
                    val isLocked = population < building.unlockPopulation
                    val canAfford = currentFunds >= building.cost
                    val isChosen = selectedBuilding == building

                    BuildingCard(
                        building = building,
                        isChosen = isChosen,
                        isLocked = isLocked,
                        canAfford = canAfford,
                        onClick = {
                            if (!isLocked) {
                                if (isBulldozeMode) onToggleBulldoze()
                                onSelectBuilding(if (isChosen) null else building)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BuildingCard(
    building: BuildingType,
    isChosen: Boolean,
    isLocked: Boolean,
    canAfford: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isChosen -> MaterialTheme.colorScheme.primary
        isLocked -> Color.Gray.copy(alpha = 0.3f)
        !canAfford -> Color(0xFFEF4444).copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Box(
        modifier = Modifier
            .width(135.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isChosen) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.surfaceContainer
            )
            .border(width = if (isChosen) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(14.dp))
            .clickable(enabled = !isLocked) { onClick() }
            .padding(10.dp)
            .testTag("building_card_${building.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${building.cost}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = if (canAfford) Color(0xFF10B981) else Color(0xFFEF4444)
                )

                if (isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                } else if (isChosen) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = building.displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1,
                color = if (isLocked) Color.Gray else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Quick Stats summary (Pop, Power, Water, Revenue)
            val statSummary = when {
                isLocked -> "Needs ${building.unlockPopulation} pop"
                building.populationBonus > 0 -> "+${building.populationBonus} citizens"
                building.powerSupply > 0 -> "+${building.powerSupply} MW power"
                building.waterSupply > 0 -> "+${building.waterSupply} kGal water"
                building.dailyRevenue > 0 -> "+$${building.dailyRevenue}/day"
                building.happinessBonus > 0 -> "+${building.happinessBonus}% happy"
                else -> "-$${building.dailyUpkeep}/day upkeep"
            }

            Text(
                text = statSummary,
                fontSize = 10.sp,
                color = if (isLocked) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

private fun getCategoryIcon(category: BuildingCategory): ImageVector = when (category) {
    BuildingCategory.ROADS -> Icons.Default.DirectionsCar
    BuildingCategory.RESIDENTIAL -> Icons.Default.Home
    BuildingCategory.COMMERCIAL -> Icons.Default.Storefront
    BuildingCategory.INDUSTRIAL -> Icons.Default.Factory
    BuildingCategory.SERVICES -> Icons.Default.LocalHospital
    BuildingCategory.LANDMARKS -> Icons.Default.Apartment
}
