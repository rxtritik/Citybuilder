package com.example.simulation

import com.example.model.BuildingCategory
import com.example.model.BuildingType
import com.example.model.CityStats
import com.example.model.Tile
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object CitySimulationEngine {

    data class SimulationStepResult(
        val updatedGrid: List<List<Tile>>,
        val updatedStats: CityStats,
        val newlyUnlockedMilestones: List<String>
    )

    fun runTick(
        currentGrid: List<List<Tile>>,
        currentStats: CityStats,
        unlockedMilestones: Set<String>,
        deltaSeconds: Float
    ): SimulationStepResult {
        val size = currentGrid.size
        var totalPowerSupply = 0
        var totalPowerDemand = 0
        var totalWaterSupply = 0
        var totalWaterDemand = 0
        var totalJobs = 0
        var rawPopulation = 0
        var totalBuildingRevenue = 0L
        var totalUpkeep = 0L

        // Step 1: Calculate global supplies and demands
        for (y in 0 until size) {
            for (x in 0 until size) {
                val tile = currentGrid[y][x]
                val b = tile.building ?: continue
                val mult = levelMultiplier(tile.level)

                totalPowerSupply += (b.powerSupply * mult).toInt()
                totalPowerDemand += (b.powerDemand * mult).toInt()
                totalWaterSupply += (b.waterSupply * mult).toInt()
                totalWaterDemand += (b.waterDemand * mult).toInt()

                totalJobs += (b.jobsProvided * mult).toInt()
                totalUpkeep += (b.dailyUpkeep * mult).toLong()

                if (b.category == BuildingCategory.RESIDENTIAL) {
                    rawPopulation += (b.populationBonus * mult).toInt()
                } else if (b.category == BuildingCategory.COMMERCIAL || b.category == BuildingCategory.INDUSTRIAL || b.category == BuildingCategory.LANDMARKS) {
                    totalBuildingRevenue += (b.dailyRevenue * mult).toLong()
                }
            }
        }

        val hasEnoughPower = totalPowerSupply >= totalPowerDemand || totalPowerDemand == 0
        val hasEnoughWater = totalWaterSupply >= totalWaterDemand || totalWaterDemand == 0

        // Step 2: Build pollution and service influence maps
        val pollutionMap = Array(size) { IntArray(size) }
        val parkBonusMap = Array(size) { IntArray(size) }
        val serviceBonusMap = Array(size) { IntArray(size) }

        for (y in 0 until size) {
            for (x in 0 until size) {
                val b = currentGrid[y][x].building ?: continue
                if (b.pollutionLevel > 0) {
                    val radius = if (b == BuildingType.COAL_PLANT) 3 else 2
                    for (dy in -radius..radius) {
                        for (dx in -radius..radius) {
                            val ny = y + dy
                            val nx = x + dx
                            if (ny in 0 until size && nx in 0 until size) {
                                val dist = abs(dx) + abs(dy)
                                if (dist <= radius) {
                                    pollutionMap[ny][nx] += max(0, b.pollutionLevel - dist * 2)
                                }
                            }
                        }
                    }
                }

                if (b.serviceRadius > 0) {
                    val radius = b.serviceRadius
                    for (dy in -radius..radius) {
                        for (dx in -radius..radius) {
                            val ny = y + dy
                            val nx = x + dx
                            if (ny in 0 until size && nx in 0 until size) {
                                val dist = abs(dx) + abs(dy)
                                if (dist <= radius) {
                                    if (b.category == BuildingCategory.SERVICES && (b == BuildingType.PARK_SMALL || b == BuildingType.BOTANICAL_GARDEN)) {
                                        parkBonusMap[ny][nx] += b.happinessBonus
                                    } else {
                                        serviceBonusMap[ny][nx] += b.happinessBonus
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Step 3: Check road access and update tile operational flags
        val newGrid = mutableListOf<List<Tile>>()
        var residentialHappinessSum = 0
        var residentialTileCount = 0

        for (y in 0 until size) {
            val newRow = mutableListOf<Tile>()
            for (x in 0 until size) {
                val oldTile = currentGrid[y][x]
                val b = oldTile.building

                if (b == null) {
                    newRow.add(oldTile.copy(pollution = pollutionMap[y][x]))
                    continue
                }

                val hasRoad = isAdjacentToRoadOrBridge(currentGrid, x, y, size)
                val isPowered = hasEnoughPower || b.powerDemand == 0
                val isWatered = hasEnoughWater || b.waterDemand == 0

                if (b.category == BuildingCategory.RESIDENTIAL) {
                    residentialTileCount++
                    var tileHappy = 80
                    if (!isPowered) tileHappy -= 25
                    if (!isWatered) tileHappy -= 25
                    if (!hasRoad) tileHappy -= 15

                    val pollution = pollutionMap[y][x]
                    tileHappy -= min(25, pollution * 3)

                    tileHappy += min(15, parkBonusMap[y][x])
                    tileHappy += min(15, serviceBonusMap[y][x])

                    residentialHappinessSum += tileHappy.coerceIn(10, 100)
                }

                newRow.add(
                    oldTile.copy(
                        isPowered = isPowered,
                        isWatered = isWatered,
                        hasRoadAccess = hasRoad,
                        pollution = pollutionMap[y][x]
                    )
                )
            }
            newGrid.add(newRow)
        }

        // Step 4: Overall Happiness Calculation
        var baseHappiness = if (residentialTileCount > 0) {
            residentialHappinessSum / residentialTileCount
        } else {
            85
        }

        // Tax rate influence on happiness
        val tax = currentStats.taxRate
        if (tax > 10) {
            baseHappiness -= (tax - 10) * 2
        } else if (tax < 9) {
            baseHappiness += (9 - tax) * 2
        }
        val finalHappiness = baseHappiness.coerceIn(15, 100)

        // Effective population adjusted by happiness and utilities
        val effectivePopulation = if (hasEnoughPower && hasEnoughWater) {
            (rawPopulation * (finalHappiness / 100f)).toInt()
        } else {
            (rawPopulation * 0.5f * (finalHappiness / 100f)).toInt()
        }

        // Step 5: Finances (daily calculation scaled to deltaSeconds)
        // One in-game day = 30 real seconds at 1x speed
        val dayFraction = deltaSeconds / 30f
        val taxRateFactor = currentStats.taxRate / 10f
        val taxRevenue = (effectivePopulation * 1.5 * taxRateFactor * (finalHappiness / 100f)).toLong()
        val totalDailyIncome = taxRevenue + (totalBuildingRevenue * (finalHappiness / 100f)).toLong()
        val netDaily = totalDailyIncome - totalUpkeep

        val fundsDelta = (netDaily * dayFraction).toLong()
        val updatedFunds = max(0L, currentStats.funds + fundsDelta)

        // Time progression: 24 hours per 30 seconds
        val hoursAdvanced = (24f / 30f) * deltaSeconds
        var newTime = currentStats.timeOfDay + hoursAdvanced
        var newDay = currentStats.day
        if (newTime >= 24f) {
            newTime -= 24f
            newDay += 1
        }

        val updatedStats = currentStats.copy(
            funds = updatedFunds,
            population = effectivePopulation,
            happiness = finalHappiness,
            powerSupply = totalPowerSupply,
            powerDemand = totalPowerDemand,
            waterSupply = totalWaterSupply,
            waterDemand = totalWaterDemand,
            dailyIncome = totalDailyIncome,
            dailyExpense = totalUpkeep,
            netDaily = netDaily,
            day = newDay,
            timeOfDay = newTime,
            totalPlaySeconds = currentStats.totalPlaySeconds + deltaSeconds.toLong()
        )

        // Step 6: Check newly unlocked milestones
        val newlyUnlocked = mutableListOf<String>()
        val milestonesToCheck = listOf(
            "pioneer" to 1,
            "hamlet" to 50,
            "village" to 150,
            "city" to 400,
            "metropolis" to 1000,
            "megacity" to 2500
        )
        for ((mId, targetPop) in milestonesToCheck) {
            if (!unlockedMilestones.contains(mId) && effectivePopulation >= targetPop) {
                newlyUnlocked.add(mId)
            }
        }

        return SimulationStepResult(
            updatedGrid = newGrid,
            updatedStats = updatedStats,
            newlyUnlockedMilestones = newlyUnlocked
        )
    }

    private fun isAdjacentToRoadOrBridge(grid: List<List<Tile>>, x: Int, y: Int, size: Int): Boolean {
        val directions = listOf(0 to 1, 0 to -1, 1 to 0, -1 to 0)
        for ((dx, dy) in directions) {
            val nx = x + dx
            val ny = y + dy
            if (nx in 0 until size && ny in 0 until size) {
                val b = grid[ny][nx].building
                if (b == BuildingType.ROAD || b == BuildingType.BRIDGE) {
                    return true
                }
            }
        }
        return false
    }

    fun levelMultiplier(level: Int): Float = when (level) {
        1 -> 1.0f
        2 -> 1.7f
        3 -> 2.6f
        else -> 1.0f
    }
}
