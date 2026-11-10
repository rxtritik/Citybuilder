package com.example.data

import com.example.model.BuildingType
import com.example.model.CityStats
import com.example.model.TerrainType
import com.example.model.Tile
import org.json.JSONArray
import org.json.JSONObject

object GameSerializer {

    fun serializeGrid(grid: List<List<Tile>>): String {
        val root = JSONArray()
        for (row in grid) {
            val rowArray = JSONArray()
            for (tile in row) {
                val tileObj = JSONObject()
                tileObj.put("x", tile.x)
                tileObj.put("y", tile.y)
                tileObj.put("t", tile.terrain.name)
                tile.building?.let { tileObj.put("b", it.id) }
                tileObj.put("l", tile.level)
                tileObj.put("p", tile.isPowered)
                tileObj.put("w", tile.isWatered)
                tileObj.put("r", tile.hasRoadAccess)
                tileObj.put("v", tile.variant)
                tileObj.put("pol", tile.pollution)
                rowArray.put(tileObj)
            }
            root.put(rowArray)
        }
        return root.toString()
    }

    fun deserializeGrid(json: String, gridSize: Int = 12): List<List<Tile>> {
        return try {
            val root = JSONArray(json)
            val result = mutableListOf<List<Tile>>()
            for (y in 0 until root.length()) {
                val rowArray = root.getJSONArray(y)
                val rowList = mutableListOf<Tile>()
                for (x in 0 until rowArray.length()) {
                    val tileObj = rowArray.getJSONObject(x)
                    val terrainStr = tileObj.optString("t", TerrainType.PLAINS.name)
                    val terrain = try {
                        TerrainType.valueOf(terrainStr)
                    } catch (e: Exception) {
                        TerrainType.PLAINS
                    }
                    val buildingId = if (tileObj.has("b")) tileObj.getString("b") else null
                    val building = BuildingType.fromId(buildingId)
                    val level = tileObj.optInt("l", 1)
                    val isPowered = tileObj.optBoolean("p", false)
                    val isWatered = tileObj.optBoolean("w", false)
                    val hasRoad = tileObj.optBoolean("r", false)
                    val variant = tileObj.optInt("v", 0)
                    val pollution = tileObj.optInt("pol", 0)

                    rowList.add(
                        Tile(
                            x = x,
                            y = y,
                            terrain = terrain,
                            building = building,
                            level = level,
                            isPowered = isPowered,
                            isWatered = isWatered,
                            hasRoadAccess = hasRoad,
                            variant = variant,
                            pollution = pollution
                        )
                    )
                }
                result.add(rowList)
            }
            if (result.isNotEmpty()) result else generateDefaultGrid(gridSize)
        } catch (e: Exception) {
            e.printStackTrace()
            generateDefaultGrid(gridSize)
        }
    }

    fun serializeMilestones(milestones: Set<String>): String {
        val array = JSONArray()
        milestones.forEach { array.put(it) }
        return array.toString()
    }

    fun deserializeMilestones(json: String): Set<String> {
        val set = mutableSetOf<String>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                set.add(array.getString(i))
            }
        } catch (e: Exception) {
            // empty on error
        }
        return set
    }

    fun generateDefaultGrid(size: Int = 12, preset: String = "River Valley"): List<List<Tile>> {
        val grid = mutableListOf<List<Tile>>()
        for (y in 0 until size) {
            val row = mutableListOf<Tile>()
            for (x in 0 until size) {
                val terrain = when (preset) {
                    "River Valley" -> {
                        // Meandering river through middle
                        val riverX = 5 + ((y % 4) - 1)
                        if (x == riverX) {
                            TerrainType.WATER
                        } else if ((x == 1 && y == 2) || (x == 2 && y == 2) || (x == 9 && y == 8) || (x == 10 && y == 9)) {
                            TerrainType.FOREST
                        } else if ((x == 0 && y == 0) || (x == 0 && y == 11)) {
                            TerrainType.MOUNTAIN
                        } else {
                            TerrainType.PLAINS
                        }
                    }
                    "Twin Lakes" -> {
                        val inLake1 = (x in 3..4 && y in 3..4)
                        val inLake2 = (x in 8..9 && y in 7..8)
                        if (inLake1 || inLake2) TerrainType.WATER
                        else if (x in 0..1 && y in 0..3) TerrainType.FOREST
                        else TerrainType.PLAINS
                    }
                    "Green Plains" -> {
                        if ((x + y) % 7 == 0 && x > 2 && y > 2) TerrainType.FOREST
                        else TerrainType.PLAINS
                    }
                    else -> TerrainType.PLAINS
                }

                // Add starting road and starter home on default map
                var building: BuildingType? = null
                if (preset == "River Valley") {
                    if (y == 6 && x in 1..4) {
                        building = BuildingType.ROAD
                    } else if (y == 5 && x == 2) {
                        building = BuildingType.HOUSE_SMALL
                    } else if (y == 7 && x == 2) {
                        building = BuildingType.WIND_TURBINE
                    } else if (y == 7 && x == 3) {
                        building = BuildingType.WATER_TOWER
                    }
                }

                row.add(
                    Tile(
                        x = x,
                        y = y,
                        terrain = terrain,
                        building = building,
                        variant = (x * 7 + y * 13) % 4
                    )
                )
            }
            grid.add(row)
        }
        return grid
    }

    fun exportSaveToJson(
        stats: CityStats,
        grid: List<List<Tile>>,
        unlockedMilestones: Set<String>
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("cityName", stats.cityName)
        root.put("mayorName", stats.mayorName)
        root.put("funds", stats.funds)
        root.put("population", stats.population)
        root.put("happiness", stats.happiness)
        root.put("day", stats.day)
        root.put("timeOfDay", stats.timeOfDay.toDouble())
        root.put("taxRate", stats.taxRate)
        root.put("totalPlaySeconds", stats.totalPlaySeconds)
        root.put("grid", serializeGrid(grid))
        root.put("milestones", serializeMilestones(unlockedMilestones))
        return root.toString(2)
    }
}
