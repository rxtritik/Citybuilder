package com.example.data

import com.example.model.CityStats
import com.example.model.Tile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SaveGameRepository(private val dao: SaveGameDao) {

    val allSaveSlots: Flow<List<SaveGameEntity>> = dao.getAllSaveSlots()

    suspend fun getSlot(slotKey: String): SaveGameEntity? = withContext(Dispatchers.IO) {
        dao.getSaveSlotByKey(slotKey)
    }

    suspend fun autoSave(
        stats: CityStats,
        grid: List<List<Tile>>,
        unlockedMilestones: Set<String>
    ) = withContext(Dispatchers.IO) {
        val entity = SaveGameEntity(
            slotKey = "autosave",
            slotTitle = "Auto-Save (${stats.cityName})",
            cityName = stats.cityName,
            mayorName = stats.mayorName,
            funds = stats.funds,
            population = stats.population,
            happiness = stats.happiness,
            dayCount = stats.day,
            timeOfDay = stats.timeOfDay,
            taxRate = stats.taxRate,
            gridDataJson = GameSerializer.serializeGrid(grid),
            unlockedMilestonesJson = GameSerializer.serializeMilestones(unlockedMilestones),
            totalPlaySeconds = stats.totalPlaySeconds,
            lastSavedTimestamp = System.currentTimeMillis()
        )
        dao.insertOrUpdate(entity)
    }

    suspend fun saveToSlot(
        slotKey: String,
        slotTitle: String,
        stats: CityStats,
        grid: List<List<Tile>>,
        unlockedMilestones: Set<String>
    ) = withContext(Dispatchers.IO) {
        val entity = SaveGameEntity(
            slotKey = slotKey,
            slotTitle = slotTitle,
            cityName = stats.cityName,
            mayorName = stats.mayorName,
            funds = stats.funds,
            population = stats.population,
            happiness = stats.happiness,
            dayCount = stats.day,
            timeOfDay = stats.timeOfDay,
            taxRate = stats.taxRate,
            gridDataJson = GameSerializer.serializeGrid(grid),
            unlockedMilestonesJson = GameSerializer.serializeMilestones(unlockedMilestones),
            totalPlaySeconds = stats.totalPlaySeconds,
            lastSavedTimestamp = System.currentTimeMillis()
        )
        dao.insertOrUpdate(entity)
    }

    suspend fun deleteSlot(slotKey: String) = withContext(Dispatchers.IO) {
        dao.deleteSlot(slotKey)
    }

    suspend fun hasExistingSaves(): Boolean = withContext(Dispatchers.IO) {
        dao.getSaveSlotCount() > 0
    }

    fun parseImportedSave(json: String): SaveGameEntity? {
        return try {
            val root = JSONObject(json)
            val cityName = root.optString("cityName", "Imported City")
            val mayorName = root.optString("mayorName", "Mayor")
            val funds = root.optLong("funds", 2000L)
            val population = root.optInt("population", 0)
            val happiness = root.optInt("happiness", 80)
            val day = root.optInt("day", 1)
            val timeOfDay = root.optDouble("timeOfDay", 10.0).toFloat()
            val taxRate = root.optInt("taxRate", 9)
            val totalSeconds = root.optLong("totalPlaySeconds", 0L)
            val gridJson = root.getString("grid")
            val milestonesJson = root.optString("milestones", "[]")

            SaveGameEntity(
                slotKey = "imported_${System.currentTimeMillis()}",
                slotTitle = "Imported: $cityName",
                cityName = cityName,
                mayorName = mayorName,
                funds = funds,
                population = population,
                happiness = happiness,
                dayCount = day,
                timeOfDay = timeOfDay,
                taxRate = taxRate,
                gridDataJson = gridJson,
                unlockedMilestonesJson = milestonesJson,
                totalPlaySeconds = totalSeconds,
                lastSavedTimestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
