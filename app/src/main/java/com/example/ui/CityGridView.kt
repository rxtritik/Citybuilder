package com.example.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import com.example.model.BuildingCategory
import com.example.model.BuildingType
import com.example.model.TerrainType
import com.example.model.Tile
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CityGridView(
    grid: List<List<Tile>>,
    selectedTile: Pair<Int, Int>?,
    activeBuildingTool: BuildingType?,
    isBulldozeMode: Boolean,
    timeOfDay: Float,
    onTileClicked: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridSize = grid.size
    val tileSize = 72f

    var scale by remember { mutableFloatStateOf(1.0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Animation ticks for water shimmer, cars, turbine rotation
    val infiniteTransition = rememberInfiniteTransition(label = "CityAnim")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    // Calculate ambient color filter based on time of day
    val ambientNightAlpha = when {
        timeOfDay < 5.0f || timeOfDay >= 21.0f -> 0.65f
        timeOfDay in 5.0f..7.0f -> ((7.0f - timeOfDay) / 2.0f) * 0.65f
        timeOfDay in 18.0f..21.0f -> ((timeOfDay - 18.0f) / 3.0f) * 0.65f
        else -> 0.0f
    }
    val sunsetAlpha = if (timeOfDay in 17.0f..19.5f) {
        (1.0f - kotlin.math.abs(timeOfDay - 18.2f) / 1.3f).coerceIn(0f, 0.45f)
    } else 0f

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.6f, 2.8f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
                .pointerInput(grid, scale, offsetX, offsetY) {
                    detectTapGestures { tapOffset ->
                        val originX = (size.width - gridSize * tileSize * scale) / 2f + offsetX
                        val originY = (size.height - gridSize * tileSize * scale) / 2f + offsetY

                        val localX = (tapOffset.x - originX) / scale
                        val localY = (tapOffset.y - originY) / scale

                        val tileX = (localX / tileSize).toInt()
                        val tileY = (localY / tileSize).toInt()

                        if (tileX in 0 until gridSize && tileY in 0 until gridSize) {
                            onTileClicked(tileX, tileY)
                        }
                    }
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // Center the city grid on screen
            val gridPixelWidth = gridSize * tileSize * scale
            val gridPixelHeight = gridSize * tileSize * scale
            val startX = (canvasW - gridPixelWidth) / 2f + offsetX
            val startY = (canvasH - gridPixelHeight) / 2f + offsetY

            withTransform({
                translate(left = startX, top = startY)
                scale(scale, scale, pivot = Offset.Zero)
            }) {
                // 1. Draw Grid Ground
                for (y in 0 until gridSize) {
                    for (x in 0 until gridSize) {
                        val tile = grid[y][x]
                        drawTileGround(tile, x, y, tileSize, animProgress)
                    }
                }

                // 2. Draw Roads and Bridges with connections
                for (y in 0 until gridSize) {
                    for (x in 0 until gridSize) {
                        val tile = grid[y][x]
                        if (tile.building == BuildingType.ROAD || tile.building == BuildingType.BRIDGE) {
                            drawRoadOrBridge(grid, x, y, tileSize, animProgress)
                        }
                    }
                }

                // 3. Draw Buildings, Flora, Structures
                for (y in 0 until gridSize) {
                    for (x in 0 until gridSize) {
                        val tile = grid[y][x]
                        val b = tile.building
                        if (b != null && b != BuildingType.ROAD && b != BuildingType.BRIDGE) {
                            drawBuilding(tile, b, x, y, tileSize, animProgress, ambientNightAlpha > 0.2f)
                        } else if (b == null && tile.terrain == TerrainType.FOREST) {
                            drawForestTrees(x, y, tileSize, tile.variant)
                        } else if (b == null && tile.terrain == TerrainType.MOUNTAIN) {
                            drawMountain(x, y, tileSize)
                        }
                    }
                }

                // 4. Draw Tile Warnings (No power, no water, isolated)
                for (y in 0 until gridSize) {
                    for (x in 0 until gridSize) {
                        val tile = grid[y][x]
                        val b = tile.building
                        if (b != null && b != BuildingType.ROAD && b != BuildingType.BRIDGE) {
                            drawStatusIndicators(tile, x, y, tileSize)
                        }
                    }
                }

                // 5. Draw Selected Tile Highlight
                if (selectedTile != null) {
                    val (sx, sy) = selectedTile
                    if (sx in 0 until gridSize && sy in 0 until gridSize) {
                        val px = sx * tileSize
                        val py = sy * tileSize
                        val highlightColor = if (isBulldozeMode) Color(0xFFEF4444) else Color(0xFF38BDF8)
                        drawRoundRect(
                            color = highlightColor,
                            topLeft = Offset(px, py),
                            size = Size(tileSize, tileSize),
                            cornerRadius = CornerRadius(8f, 8f),
                            style = Stroke(width = 3.5f)
                        )
                        drawRoundRect(
                            color = highlightColor.copy(alpha = 0.22f),
                            topLeft = Offset(px, py),
                            size = Size(tileSize, tileSize),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                    }
                }

                // 6. Day/Sunset/Night Ambient Overlay on Map
                if (sunsetAlpha > 0f) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFF7A00).copy(alpha = sunsetAlpha), Color(0xFFFF4500).copy(alpha = sunsetAlpha * 0.7f))
                        ),
                        topLeft = Offset(0f, 0f),
                        size = Size(gridSize * tileSize, gridSize * tileSize)
                    )
                }
                if (ambientNightAlpha > 0f) {
                    drawRect(
                        color = Color(0xFF03071E).copy(alpha = ambientNightAlpha),
                        topLeft = Offset(0f, 0f),
                        size = Size(gridSize * tileSize, gridSize * tileSize)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawTileGround(
    tile: Tile,
    x: Int,
    y: Int,
    tileSize: Float,
    animProgress: Float
) {
    val px = x * tileSize
    val py = y * tileSize

    when (tile.terrain) {
        TerrainType.PLAINS -> {
            // Subtle checker pattern for grid feel
            val baseGreen = if ((x + y) % 2 == 0) Color(0xFF4ADE80) else Color(0xFF3FCF75)
            drawRect(
                color = baseGreen,
                topLeft = Offset(px, py),
                size = Size(tileSize, tileSize)
            )
            // Delicate tile boundary
            drawRect(
                color = Color(0xFF22C55E).copy(alpha = 0.45f),
                topLeft = Offset(px, py),
                size = Size(tileSize, tileSize),
                style = Stroke(width = 1f)
            )
        }
        TerrainType.WATER -> {
            val waterBase = Color(0xFF0284C7)
            val waterLight = Color(0xFF38BDF8)
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(waterBase, waterLight),
                    start = Offset(px, py),
                    end = Offset(px + tileSize, py + tileSize)
                ),
                topLeft = Offset(px, py),
                size = Size(tileSize, tileSize)
            )
            // Shimmer waves
            val waveOffset = (animProgress * tileSize * 1.5f) % tileSize
            val waveY = py + (waveOffset + (x * 13) % tileSize) % tileSize
            drawLine(
                color = Color.White.copy(alpha = 0.35f),
                start = Offset(px + 10f, waveY),
                end = Offset(px + tileSize - 10f, waveY),
                strokeWidth = 2.5f
            )
        }
        TerrainType.FOREST -> {
            drawRect(
                color = Color(0xFF2E8B57),
                topLeft = Offset(px, py),
                size = Size(tileSize, tileSize)
            )
        }
        TerrainType.MOUNTAIN -> {
            drawRect(
                color = Color(0xFF475569),
                topLeft = Offset(px, py),
                size = Size(tileSize, tileSize)
            )
        }
    }
}

private fun DrawScope.drawRoadOrBridge(
    grid: List<List<Tile>>,
    x: Int,
    y: Int,
    tileSize: Float,
    animProgress: Float
) {
    val px = x * tileSize
    val py = y * tileSize
    val half = tileSize / 2f
    val roadWidth = tileSize * 0.45f
    val roadHalf = roadWidth / 2f

    val isBridge = grid[y][x].building == BuildingType.BRIDGE
    val roadColor = if (isBridge) Color(0xFF78350F) else Color(0xFF27272A)
    val centerColor = if (isBridge) Color(0xFFD97706) else Color(0xFFFACC15)

    // Center road block
    drawRect(
        color = roadColor,
        topLeft = Offset(px + half - roadHalf, py + half - roadHalf),
        size = Size(roadWidth, roadWidth)
    )

    // Detect connections with adjacent roads/bridges
    val size = grid.size
    val hasNorth = y > 0 && isTransit(grid[y - 1][x])
    val hasSouth = y < size - 1 && isTransit(grid[y + 1][x])
    val hasWest = x > 0 && isTransit(grid[y][x - 1])
    val hasEast = x < size - 1 && isTransit(grid[y][x + 1])

    if (hasNorth) {
        drawRect(
            color = roadColor,
            topLeft = Offset(px + half - roadHalf, py),
            size = Size(roadWidth, half)
        )
        drawLine(
            color = centerColor,
            start = Offset(px + half, py),
            end = Offset(px + half, py + half),
            strokeWidth = 2f
        )
    }
    if (hasSouth) {
        drawRect(
            color = roadColor,
            topLeft = Offset(px + half - roadHalf, py + half),
            size = Size(roadWidth, half)
        )
        drawLine(
            color = centerColor,
            start = Offset(px + half, py + half),
            end = Offset(px + half, py + tileSize),
            strokeWidth = 2f
        )
    }
    if (hasWest) {
        drawRect(
            color = roadColor,
            topLeft = Offset(px, py + half - roadHalf),
            size = Size(half, roadWidth)
        )
        drawLine(
            color = centerColor,
            start = Offset(px, py + half),
            end = Offset(px + half, py + half),
            strokeWidth = 2f
        )
    }
    if (hasEast) {
        drawRect(
            color = roadColor,
            topLeft = Offset(px + half, py + half - roadHalf),
            size = Size(half, roadWidth)
        )
        drawLine(
            color = centerColor,
            start = Offset(px + half, py + half),
            end = Offset(px + tileSize, py + half),
            strokeWidth = 2f
        )
    }

    // Isolated road shows full dot
    if (!hasNorth && !hasSouth && !hasWest && !hasEast) {
        drawCircle(
            color = centerColor,
            radius = 3.5f,
            center = Offset(px + half, py + half)
        )
    }

    // Little animated cars cruising
    if ((x + y) % 3 == 0) {
        val carPos = (animProgress * 1.5f + (x * 0.2f)) % 1f
        if (hasWest && hasEast) {
            val cx = px + carPos * tileSize
            val cy = py + half + 3f
            drawRoundRect(
                color = Color(0xFFEF4444),
                topLeft = Offset(cx - 5f, cy - 2.5f),
                size = Size(10f, 5f),
                cornerRadius = CornerRadius(1.5f, 1.5f)
            )
            // Headlights
            drawCircle(
                color = Color(0xFFFEF08A),
                radius = 1.5f,
                center = Offset(cx + 6f, cy)
            )
        } else if (hasNorth && hasSouth) {
            val cx = px + half + 3f
            val cy = py + carPos * tileSize
            drawRoundRect(
                color = Color(0xFF3B82F6),
                topLeft = Offset(cx - 2.5f, cy - 5f),
                size = Size(5f, 10f),
                cornerRadius = CornerRadius(1.5f, 1.5f)
            )
        }
    }
}

private fun isTransit(tile: Tile): Boolean {
    val b = tile.building
    return b == BuildingType.ROAD || b == BuildingType.BRIDGE
}

private fun DrawScope.drawBuilding(
    tile: Tile,
    b: BuildingType,
    x: Int,
    y: Int,
    tileSize: Float,
    animProgress: Float,
    isNight: Boolean
) {
    val px = x * tileSize
    val py = y * tileSize
    val pad = tileSize * 0.12f
    val bW = tileSize - pad * 2
    val bH = tileSize - pad * 2

    // Shadow underneath
    drawRoundRect(
        color = Color(0x33000000),
        topLeft = Offset(px + pad + 3f, py + pad + 4f),
        size = Size(bW, bH),
        cornerRadius = CornerRadius(6f, 6f)
    )

    when (b) {
        BuildingType.HOUSE_SMALL -> {
            // Cute suburban roof & walls
            val roofColor = if (tile.variant % 2 == 0) Color(0xFFDC2626) else Color(0xFF2563EB)
            drawRoundRect(
                color = Color(0xFFFDE68A),
                topLeft = Offset(px + pad + 4f, py + pad + 8f),
                size = Size(bW - 8f, bH - 12f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Roof
            val path = Path().apply {
                moveTo(px + pad + 2f, py + pad + 10f)
                lineTo(px + tileSize / 2f, py + pad + 2f)
                lineTo(px + tileSize - pad - 2f, py + pad + 10f)
                close()
            }
            drawPath(path, roofColor)
            // Window with night glow
            val winColor = if (isNight) Color(0xFFFDE047) else Color(0xFF93C5FD)
            drawRect(
                color = winColor,
                topLeft = Offset(px + tileSize / 2f - 4f, py + pad + 14f),
                size = Size(8f, 7f)
            )
        }

        BuildingType.HOUSE_MODERN -> {
            // Stylish duplex
            drawRoundRect(
                color = Color(0xFFE2E8F0),
                topLeft = Offset(px + pad + 2f, py + pad + 4f),
                size = Size(bW - 4f, bH - 6f),
                cornerRadius = CornerRadius(5f, 5f)
            )
            drawRoundRect(
                color = Color(0xFF0284C7),
                topLeft = Offset(px + pad + 4f, py + pad + 6f),
                size = Size(bW - 8f, 8f),
                cornerRadius = CornerRadius(3f, 3f)
            )
            // Windows
            val winColor = if (isNight) Color(0xFFFDE047) else Color(0xFF38BDF8)
            drawRect(color = winColor, topLeft = Offset(px + pad + 6f, py + pad + 18f), size = Size(10f, 10f))
            drawRect(color = winColor, topLeft = Offset(px + tileSize - pad - 16f, py + pad + 18f), size = Size(10f, 10f))
        }

        BuildingType.RESIDENTIAL_TOWER -> {
            // Tall skyscraper
            val glassColor = Color(0xFF0F172A)
            drawRoundRect(
                color = glassColor,
                topLeft = Offset(px + pad + 4f, py + pad - 4f),
                size = Size(bW - 8f, bH + 6f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Grid of glowing windows
            val winColor = if (isNight) Color(0xFFFDE047) else Color(0xFF67E8F9)
            for (wy in 0..4) {
                for (wx in 0..2) {
                    drawRect(
                        color = winColor,
                        topLeft = Offset(px + pad + 8f + wx * 10f, py + pad + 2f + wy * 8f),
                        size = Size(6f, 5f)
                    )
                }
            }
        }

        BuildingType.SHOP_LOCAL -> {
            // Market with striped awning
            drawRoundRect(
                color = Color(0xFFFEF3C7),
                topLeft = Offset(px + pad + 2f, py + pad + 6f),
                size = Size(bW - 4f, bH - 8f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Striped awning
            for (i in 0..5) {
                val stripeColor = if (i % 2 == 0) Color(0xFFEF4444) else Color.White
                drawRect(
                    color = stripeColor,
                    topLeft = Offset(px + pad + 2f + i * ((bW - 4f) / 6f), py + pad + 8f),
                    size = Size((bW - 4f) / 6f, 8f)
                )
            }
        }

        BuildingType.COMMERCIAL_MALL -> {
            // Shopping Mall
            drawRoundRect(
                color = Color(0xFF64748B),
                topLeft = Offset(px + pad + 2f, py + pad + 2f),
                size = Size(bW - 4f, bH - 4f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Glass Atrium Dome
            drawCircle(
                color = Color(0xFF38BDF8),
                radius = 12f,
                center = Offset(px + tileSize / 2f, py + tileSize / 2f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = 7f,
                center = Offset(px + tileSize / 2f - 3f, py + tileSize / 2f - 3f)
            )
        }

        BuildingType.TECH_OFFICE -> {
            // Cyber Tech Center
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(px + pad + 3f, py + pad - 2f),
                size = Size(bW - 6f, bH + 4f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Neon accent lines
            drawRoundRect(
                color = Color(0xFF38BDF8),
                topLeft = Offset(px + pad + 5f, py + pad + 4f),
                size = Size(bW - 10f, 4f),
                cornerRadius = CornerRadius(2f, 2f)
            )
            drawRoundRect(
                color = Color(0xFFA855F7),
                topLeft = Offset(px + pad + 5f, py + tileSize - pad - 12f),
                size = Size(bW - 10f, 4f),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }

        BuildingType.WIND_TURBINE -> {
            // Base pole
            drawRect(
                color = Color(0xFFCBD5E1),
                topLeft = Offset(px + tileSize / 2f - 2f, py + pad + 8f),
                size = Size(4f, bH - 8f)
            )
            val hubCenter = Offset(px + tileSize / 2f, py + pad + 8f)
            drawCircle(color = Color.White, radius = 4f, center = hubCenter)
            // Rotating 3 blades
            val angleBase = animProgress * 360f
            for (i in 0..2) {
                val angleRad = Math.toRadians((angleBase + i * 120.0)).toFloat()
                val bladeEnd = Offset(
                    hubCenter.x + cos(angleRad) * 16f,
                    hubCenter.y + sin(angleRad) * 16f
                )
                drawLine(
                    color = Color.White,
                    start = hubCenter,
                    end = bladeEnd,
                    strokeWidth = 3f
                )
            }
        }

        BuildingType.SOLAR_ARRAY -> {
            // Photovoltaic panels
            for (r in 0..1) {
                for (c in 0..1) {
                    val spx = px + pad + 4f + c * 22f
                    val spy = py + pad + 4f + r * 22f
                    drawRoundRect(
                        color = Color(0xFF1D4ED8),
                        topLeft = Offset(spx, spy),
                        size = Size(18f, 18f),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                    drawRoundRect(
                        color = Color(0xFF60A5FA),
                        topLeft = Offset(spx + 1f, spy + 1f),
                        size = Size(16f, 16f),
                        style = Stroke(width = 1f),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                }
            }
        }

        BuildingType.COAL_PLANT -> {
            // Thermal Plant with cooling tower
            drawRoundRect(
                color = Color(0xFF334155),
                topLeft = Offset(px + pad + 4f, py + pad + 10f),
                size = Size(bW - 8f, bH - 12f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Smokestack
            drawRect(
                color = Color(0xFF991B1B),
                topLeft = Offset(px + pad + 8f, py + pad),
                size = Size(8f, 14f)
            )
            // Gentle smoke puff
            val smokeAlpha = (1f - (animProgress % 0.5f) * 2f).coerceIn(0.1f, 0.7f)
            drawCircle(
                color = Color.Gray.copy(alpha = smokeAlpha),
                radius = 5f + animProgress * 4f,
                center = Offset(px + pad + 12f, py + pad - 4f - animProgress * 6f)
            )
        }

        BuildingType.WATER_TOWER -> {
            // Metal legs
            drawLine(
                color = Color(0xFF64748B),
                start = Offset(px + pad + 8f, py + tileSize - pad),
                end = Offset(px + tileSize / 2f, py + pad + 12f),
                strokeWidth = 2.5f
            )
            drawLine(
                color = Color(0xFF64748B),
                start = Offset(px + tileSize - pad - 8f, py + tileSize - pad),
                end = Offset(px + tileSize / 2f, py + pad + 12f),
                strokeWidth = 2.5f
            )
            // Water tank sphere
            drawCircle(
                color = Color(0xFF0284C7),
                radius = 13f,
                center = Offset(px + tileSize / 2f, py + pad + 12f)
            )
            drawCircle(
                color = Color(0xFF38BDF8),
                radius = 6f,
                center = Offset(px + tileSize / 2f - 3f, py + pad + 9f)
            )
        }

        BuildingType.WATER_STATION -> {
            // Treatment station
            drawRoundRect(
                color = Color(0xFF0369A1),
                topLeft = Offset(px + pad + 2f, py + pad + 4f),
                size = Size(bW - 4f, bH - 6f),
                cornerRadius = CornerRadius(5f, 5f)
            )
            drawCircle(
                color = Color(0xFF38BDF8),
                radius = 8f,
                center = Offset(px + tileSize / 2f, py + tileSize / 2f)
            )
        }

        BuildingType.FACTORY -> {
            // Sawtooth industrial roof
            drawRoundRect(
                color = Color(0xFF71717A),
                topLeft = Offset(px + pad + 4f, py + pad + 8f),
                size = Size(bW - 8f, bH - 10f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Sawtooth roof lines
            for (i in 0..2) {
                val rx = px + pad + 4f + i * 12f
                val path = Path().apply {
                    moveTo(rx, py + pad + 8f)
                    lineTo(rx + 6f, py + pad + 2f)
                    lineTo(rx + 12f, py + pad + 8f)
                }
                drawPath(path, Color(0xFF52525B))
            }
        }

        BuildingType.PARK_SMALL -> {
            // Park with trees and fountain
            drawRoundRect(
                color = Color(0xFF22C55E),
                topLeft = Offset(px + pad, py + pad),
                size = Size(bW, bH),
                cornerRadius = CornerRadius(8f, 8f)
            )
            // Water fountain in center
            drawCircle(
                color = Color(0xFF38BDF8),
                radius = 8f,
                center = Offset(px + tileSize / 2f, py + tileSize / 2f)
            )
            drawCircle(
                color = Color.White,
                radius = 3.5f,
                center = Offset(px + tileSize / 2f, py + tileSize / 2f)
            )
            // Little trees
            drawCircle(color = Color(0xFF15803D), radius = 6f, center = Offset(px + pad + 8f, py + pad + 8f))
            drawCircle(color = Color(0xFF15803D), radius = 6f, center = Offset(px + tileSize - pad - 8f, py + tileSize - pad - 8f))
        }

        BuildingType.BOTANICAL_GARDEN -> {
            // Glass conservatory
            drawRoundRect(
                color = Color(0xFF16A34A),
                topLeft = Offset(px + pad, py + pad),
                size = Size(bW, bH),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawCircle(
                color = Color(0xFFBAE6FD).copy(alpha = 0.85f),
                radius = 16f,
                center = Offset(px + tileSize / 2f, py + tileSize / 2f)
            )
            drawCircle(
                color = Color(0xFF0284C7),
                radius = 16f,
                center = Offset(px + tileSize / 2f, py + tileSize / 2f),
                style = Stroke(width = 2f)
            )
        }

        BuildingType.POLICE_STATION -> {
            drawRoundRect(
                color = Color(0xFF1E3A8A),
                topLeft = Offset(px + pad + 2f, py + pad + 4f),
                size = Size(bW - 4f, bH - 6f),
                cornerRadius = CornerRadius(5f, 5f)
            )
            // Blue siren beacon
            val beaconAlpha = if (animProgress % 0.5f > 0.25f) 1.0f else 0.3f
            drawCircle(
                color = Color(0xFF60A5FA).copy(alpha = beaconAlpha),
                radius = 4f,
                center = Offset(px + tileSize / 2f, py + pad + 4f)
            )
        }

        BuildingType.FIRE_STATION -> {
            drawRoundRect(
                color = Color(0xFFB91C1C),
                topLeft = Offset(px + pad + 2f, py + pad + 4f),
                size = Size(bW - 4f, bH - 6f),
                cornerRadius = CornerRadius(5f, 5f)
            )
            // Garage doors
            drawRect(
                color = Color(0xFF450A0A),
                topLeft = Offset(px + pad + 6f, py + pad + 16f),
                size = Size(12f, 16f)
            )
            drawRect(
                color = Color(0xFF450A0A),
                topLeft = Offset(px + tileSize - pad - 18f, py + pad + 16f),
                size = Size(12f, 16f)
            )
        }

        BuildingType.HOSPITAL -> {
            // General Hospital with red cross
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(px + pad + 2f, py + pad + 4f),
                size = Size(bW - 4f, bH - 6f),
                cornerRadius = CornerRadius(5f, 5f)
            )
            // Red cross emblem
            val cx = px + tileSize / 2f
            val cy = py + tileSize / 2f
            drawRect(color = Color(0xFFEF4444), topLeft = Offset(cx - 3f, cy - 8f), size = Size(6f, 16f))
            drawRect(color = Color(0xFFEF4444), topLeft = Offset(cx - 8f, cy - 3f), size = Size(16f, 6f))
        }

        BuildingType.SCHOOL -> {
            drawRoundRect(
                color = Color(0xFFD97706),
                topLeft = Offset(px + pad + 2f, py + pad + 4f),
                size = Size(bW - 4f, bH - 6f),
                cornerRadius = CornerRadius(5f, 5f)
            )
            // Bell tower
            drawRect(
                color = Color(0xFF78350F),
                topLeft = Offset(px + tileSize / 2f - 4f, py + pad),
                size = Size(8f, 10f)
            )
            drawCircle(
                color = Color(0xFFFDE047),
                radius = 2.5f,
                center = Offset(px + tileSize / 2f, py + pad + 5f)
            )
        }

        BuildingType.CITY_HALL -> {
            // Classical columns & golden dome
            drawRoundRect(
                color = Color(0xFFE2E8F0),
                topLeft = Offset(px + pad + 2f, py + pad + 8f),
                size = Size(bW - 4f, bH - 10f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Golden Dome
            drawCircle(
                color = Color(0xFFF59E0B),
                radius = 10f,
                center = Offset(px + tileSize / 2f, py + pad + 6f)
            )
        }

        BuildingType.LANDMARK_MONUMENT -> {
            // Cosmo Spire needle
            val cx = px + tileSize / 2f
            val path = Path().apply {
                moveTo(cx, py + pad - 6f)
                lineTo(cx - 10f, py + tileSize - pad)
                lineTo(cx + 10f, py + tileSize - pad)
                close()
            }
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF38BDF8), Color(0xFF6366F1), Color(0xFF1E1B4B))
                )
            )
            // Pulsing tip beacon
            drawCircle(
                color = Color(0xFFF43F5E),
                radius = 3.5f + (sin(animProgress * 6.28f) * 1.5f).toFloat(),
                center = Offset(cx, py + pad - 6f)
            )
        }

        else -> {
            drawRoundRect(
                color = Color(0xFF64748B),
                topLeft = Offset(px + pad, py + pad),
                size = Size(bW, bH),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
    }

    // Level stars if upgraded
    if (tile.level > 1) {
        for (lvl in 1 until tile.level) {
            val starX = px + pad + 4f + (lvl - 1) * 8f
            val starY = py + pad + 4f
            drawCircle(
                color = Color(0xFFFACC15),
                radius = 2.5f,
                center = Offset(starX, starY)
            )
        }
    }
}

private fun DrawScope.drawForestTrees(x: Int, y: Int, tileSize: Float, variant: Int) {
    val px = x * tileSize
    val py = y * tileSize
    val offsets = listOf(
        Offset(px + 18f, py + 22f),
        Offset(px + 50f, py + 26f),
        Offset(px + 34f, py + 48f),
        Offset(px + 52f, py + 56f)
    )
    for ((idx, pos) in offsets.withIndex()) {
        val radius = if ((idx + variant) % 2 == 0) 9f else 11f
        val treeColor = if (idx % 2 == 0) Color(0xFF166534) else Color(0xFF15803D)
        drawCircle(color = treeColor, radius = radius, center = pos)
        drawCircle(color = Color(0xFF22C55E).copy(alpha = 0.5f), radius = radius * 0.45f, center = Offset(pos.x - 2f, pos.y - 2f))
    }
}

private fun DrawScope.drawMountain(x: Int, y: Int, tileSize: Float) {
    val px = x * tileSize
    val py = y * tileSize
    val cx = px + tileSize / 2f
    val path = Path().apply {
        moveTo(cx, py + 12f)
        lineTo(px + 8f, py + tileSize - 8f)
        lineTo(px + tileSize - 8f, py + tileSize - 8f)
        close()
    }
    drawPath(path, Color(0xFF475569))
    // Snow cap
    val capPath = Path().apply {
        moveTo(cx, py + 12f)
        lineTo(cx - 8f, py + 26f)
        lineTo(cx + 8f, py + 26f)
        close()
    }
    drawPath(capPath, Color.White)
}

private fun DrawScope.drawStatusIndicators(tile: Tile, x: Int, y: Int, tileSize: Float) {
    val px = x * tileSize
    val py = y * tileSize
    var iconOffset = 0f

    // Power missing
    if (!tile.isPowered && (tile.building?.powerDemand ?: 0) > 0) {
        val cx = px + tileSize - 10f - iconOffset
        val cy = py + 10f
        drawCircle(color = Color(0xFFEF4444), radius = 6.5f, center = Offset(cx, cy))
        drawCircle(color = Color(0xFFFDE047), radius = 3f, center = Offset(cx, cy))
        iconOffset += 14f
    }
    // Water missing
    if (!tile.isWatered && (tile.building?.waterDemand ?: 0) > 0) {
        val cx = px + tileSize - 10f - iconOffset
        val cy = py + 10f
        drawCircle(color = Color(0xFF2563EB), radius = 6.5f, center = Offset(cx, cy))
        drawCircle(color = Color(0xFF67E8F9), radius = 3f, center = Offset(cx, cy))
        iconOffset += 14f
    }
    // No road access
    if (!tile.hasRoadAccess && tile.building?.category == BuildingCategory.RESIDENTIAL) {
        val cx = px + 10f
        val cy = py + 10f
        drawCircle(color = Color(0xFFF59E0B), radius = 6f, center = Offset(cx, cy))
        drawCircle(color = Color.Black, radius = 2f, center = Offset(cx, cy))
    }
}
