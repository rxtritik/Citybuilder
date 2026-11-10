package com.example

import com.example.data.GameSerializer
import com.example.model.BuildingType
import com.example.model.CityMilestone
import com.example.model.CityStats
import com.example.model.TerrainType
import com.example.simulation.CitySimulationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testDefaultGridGeneration() {
        val grid = GameSerializer.generateDefaultGrid(size = 12, preset = "River Valley")
        assertEquals(12, grid.size)
        assertEquals(12, grid[0].size)

        var waterCount = 0
        for (row in grid) {
            for (tile in row) {
                if (tile.terrain == TerrainType.WATER) waterCount++
            }
        }
        assertTrue("River should have water tiles", waterCount > 0)
    }

    @Test
    fun testLevelMultipliers() {
        assertEquals(1.0f, CitySimulationEngine.levelMultiplier(1), 0.01f)
        assertEquals(1.7f, CitySimulationEngine.levelMultiplier(2), 0.01f)
        assertEquals(2.6f, CitySimulationEngine.levelMultiplier(3), 0.01f)
    }

    @Test
    fun testCitySimulationTick() {
        val grid = GameSerializer.generateDefaultGrid(size = 12, preset = "River Valley")
        val stats = CityStats(funds = 2500L, population = 0, happiness = 80)
        val result = CitySimulationEngine.runTick(
            currentGrid = grid,
            currentStats = stats,
            unlockedMilestones = emptySet(),
            deltaSeconds = 1.0f
        )

        assertNotNull(result.updatedStats)
        assertTrue("Power supply should be positive", result.updatedStats.powerSupply > 0)
        assertTrue("Water supply should be positive", result.updatedStats.waterSupply > 0)
    }

    @Test
    fun testMilestonesData() {
        val milestones = CityMilestone.ALL_MILESTONES
        assertTrue(milestones.isNotEmpty())
        assertEquals("pioneer", milestones.first().id)
        assertEquals(1, milestones.first().targetPopulation)
    }
}
