package com.example.model

data class Tile(
    val x: Int,
    val y: Int,
    val terrain: TerrainType = TerrainType.PLAINS,
    val building: BuildingType? = null,
    val level: Int = 1,
    val isPowered: Boolean = false,
    val isWatered: Boolean = false,
    val hasRoadAccess: Boolean = false,
    val variant: Int = 0,
    val pollution: Int = 0
) {
    val isOccupied: Boolean
        get() = building != null

    val canBuildOn: Boolean
        get() = building == null && (terrain.isBuildable || (terrain == TerrainType.WATER && false))

    val upgradeCost: Int
        get() = building?.let { (it.cost * 0.75f * level).toInt() } ?: 0

    val maxLevel: Int = 3
}
