package com.example.model

enum class TerrainType(val displayName: String, val isWater: Boolean, val isBuildable: Boolean) {
    PLAINS("Plains", false, true),
    WATER("River/Water", true, false),
    FOREST("Forest", false, true),
    MOUNTAIN("Mountain", false, false)
}
