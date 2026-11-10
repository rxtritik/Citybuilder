package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CityDatabase
import com.example.data.GameSerializer
import com.example.data.SaveGameEntity
import com.example.data.SaveGameRepository
import com.example.model.BuildingCategory
import com.example.model.BuildingType
import com.example.model.CityMilestone
import com.example.model.CityStats
import com.example.model.TerrainType
import com.example.model.Tile
import com.example.simulation.CitySimulationEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CityGameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SaveGameRepository

    private val _grid = MutableStateFlow<List<List<Tile>>>(emptyList())
    val grid: StateFlow<List<List<Tile>>> = _grid.asStateFlow()

    private val _stats = MutableStateFlow(CityStats())
    val stats: StateFlow<CityStats> = _stats.asStateFlow()

    private val _unlockedMilestones = MutableStateFlow<Set<String>>(emptySet())
    val unlockedMilestones: StateFlow<Set<String>> = _unlockedMilestones.asStateFlow()

    private val _selectedTile = MutableStateFlow<Pair<Int, Int>?>(null)
    val selectedTile: StateFlow<Pair<Int, Int>?> = _selectedTile.asStateFlow()

    private val _activeBuildingTool = MutableStateFlow<BuildingType?>(null)
    val activeBuildingTool: StateFlow<BuildingType?> = _activeBuildingTool.asStateFlow()

    private val _isBulldozeMode = MutableStateFlow(false)
    val isBulldozeMode: StateFlow<Boolean> = _isBulldozeMode.asStateFlow()

    private val _isAutoSavedRecent = MutableStateFlow(false)
    val isAutoSavedRecent: StateFlow<Boolean> = _isAutoSavedRecent.asStateFlow()

    val saveSlots: StateFlow<List<SaveGameEntity>>

    private var gameLoopJob: Job? = null
    private var autoSaveCounter = 0

    init {
        val db = CityDatabase.getDatabase(application)
        repository = SaveGameRepository(db.saveGameDao())

        saveSlots = repository.allSaveSlots.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Initial setup: Load existing auto-save or start River Valley
        viewModelScope.launch {
            val autoSave = repository.getSlot("autosave")
            if (autoSave != null) {
                loadFromEntity(autoSave)
            } else {
                startNewCity("Neo Horizon", "Mayor", "River Valley")
            }
            startGameLoop()
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val currentSpeed = _stats.value.simulationSpeed
                if (currentSpeed > 0 && _grid.value.isNotEmpty()) {
                    val deltaSeconds = currentSpeed.toFloat()
                    val result = CitySimulationEngine.runTick(
                        currentGrid = _grid.value,
                        currentStats = _stats.value,
                        unlockedMilestones = _unlockedMilestones.value,
                        deltaSeconds = deltaSeconds
                    )
                    _grid.value = result.updatedGrid
                    _stats.value = result.updatedStats

                    // Auto-claim newly reached milestones
                    if (result.newlyUnlockedMilestones.isNotEmpty()) {
                        val currentMilestones = _unlockedMilestones.value.toMutableSet()
                        currentMilestones.addAll(result.newlyUnlockedMilestones)
                        _unlockedMilestones.value = currentMilestones
                    }

                    // Periodic auto-save every 12 ticks
                    autoSaveCounter += currentSpeed
                    if (autoSaveCounter >= 12) {
                        autoSaveCounter = 0
                        triggerAutoSave()
                    }
                }
            }
        }
    }

    fun onTileClicked(x: Int, y: Int) {
        val currentGrid = _grid.value
        if (y !in currentGrid.indices || x !in currentGrid[y].indices) return
        val tile = currentGrid[y][x]

        if (_isBulldozeMode.value) {
            // Bulldoze action
            demolishTile(x, y)
            return
        }

        val tool = _activeBuildingTool.value
        if (tool != null) {
            // Build action
            tryPlaceBuilding(x, y, tool)
        } else {
            // Select & Inspect tile
            _selectedTile.value = Pair(x, y)
        }
    }

    fun selectBuildingTool(building: BuildingType?) {
        _activeBuildingTool.value = building
        if (building != null) {
            _isBulldozeMode.value = false
            _selectedTile.value = null
        }
    }

    fun toggleBulldozeMode() {
        _isBulldozeMode.value = !_isBulldozeMode.value
        if (_isBulldozeMode.value) {
            _activeBuildingTool.value = null
            _selectedTile.value = null
        }
    }

    fun clearSelection() {
        _selectedTile.value = null
    }

    private fun tryPlaceBuilding(x: Int, y: Int, building: BuildingType) {
        val currentGrid = _grid.value.map { it.toMutableList() }.toMutableList()
        val tile = currentGrid[y][x]

        // Check funds
        if (_stats.value.funds < building.cost) return

        // Terrain validity
        val canBuild = when (building) {
            BuildingType.BRIDGE -> tile.terrain == TerrainType.WATER && tile.building == null
            BuildingType.ROAD -> (tile.terrain == TerrainType.PLAINS || tile.terrain == TerrainType.FOREST) && tile.building == null
            else -> (tile.terrain == TerrainType.PLAINS || tile.terrain == TerrainType.FOREST) && tile.building == null
        }

        if (!canBuild) return

        // Deduct cost and place
        val newFunds = _stats.value.funds - building.cost
        currentGrid[y][x] = tile.copy(
            building = building,
            level = 1,
            variant = (x * 3 + y * 7) % 4
        )

        _grid.value = currentGrid
        _stats.value = _stats.value.copy(funds = newFunds)

        // Instant simulation recalculation for immediate feedback
        recalculateSimulation()
        triggerAutoSave()
    }

    fun upgradeSelectedTile() {
        val sel = _selectedTile.value ?: return
        val (x, y) = sel
        val currentGrid = _grid.value.map { it.toMutableList() }.toMutableList()
        val tile = currentGrid[y][x]
        val b = tile.building ?: return

        if (tile.level >= tile.maxLevel) return
        val cost = tile.upgradeCost
        if (_stats.value.funds < cost) return

        currentGrid[y][x] = tile.copy(level = tile.level + 1)
        _grid.value = currentGrid
        _stats.value = _stats.value.copy(funds = _stats.value.funds - cost)

        recalculateSimulation()
        triggerAutoSave()
    }

    fun demolishSelectedTile() {
        val sel = _selectedTile.value ?: return
        demolishTile(sel.first, sel.second)
        _selectedTile.value = null
    }

    private fun demolishTile(x: Int, y: Int) {
        val currentGrid = _grid.value.map { it.toMutableList() }.toMutableList()
        val tile = currentGrid[y][x]
        val b = tile.building

        val refund = if (b != null) (b.cost * 0.4f).toLong() else 0L

        currentGrid[y][x] = tile.copy(
            building = null,
            level = 1,
            isPowered = false,
            isWatered = false,
            hasRoadAccess = false
        )

        _grid.value = currentGrid
        _stats.value = _stats.value.copy(funds = _stats.value.funds + refund)

        recalculateSimulation()
        triggerAutoSave()
    }

    private fun recalculateSimulation() {
        val result = CitySimulationEngine.runTick(
            currentGrid = _grid.value,
            currentStats = _stats.value,
            unlockedMilestones = _unlockedMilestones.value,
            deltaSeconds = 0.1f
        )
        _grid.value = result.updatedGrid
        _stats.value = result.updatedStats
    }

    fun claimMilestoneReward(milestone: CityMilestone) {
        val current = _unlockedMilestones.value.toMutableSet()
        if (!current.contains(milestone.id) && _stats.value.population >= milestone.targetPopulation) {
            current.add(milestone.id)
            _unlockedMilestones.value = current
            _stats.value = _stats.value.copy(funds = _stats.value.funds + milestone.rewardFunds)
            triggerAutoSave()
        }
    }

    fun setSimulationSpeed(speed: Int) {
        _stats.value = _stats.value.copy(simulationSpeed = speed.coerceIn(0, 3))
    }

    fun triggerAutoSave() {
        viewModelScope.launch {
            repository.autoSave(
                stats = _stats.value,
                grid = _grid.value,
                unlockedMilestones = _unlockedMilestones.value
            )
            _isAutoSavedRecent.value = true
            delay(2500L)
            _isAutoSavedRecent.value = false
        }
    }

    fun saveToSlot(slotKey: String, slotTitle: String) {
        viewModelScope.launch {
            repository.saveToSlot(
                slotKey = slotKey,
                slotTitle = slotTitle,
                stats = _stats.value,
                grid = _grid.value,
                unlockedMilestones = _unlockedMilestones.value
            )
            _isAutoSavedRecent.value = true
            delay(2500L)
            _isAutoSavedRecent.value = false
        }
    }

    fun loadSlot(slotKey: String) {
        viewModelScope.launch {
            val entity = repository.getSlot(slotKey) ?: return@launch
            loadFromEntity(entity)
        }
    }

    fun deleteSlot(slotKey: String) {
        viewModelScope.launch {
            repository.deleteSlot(slotKey)
        }
    }

    fun exportSaveJson(): String {
        return GameSerializer.exportSaveToJson(
            stats = _stats.value,
            grid = _grid.value,
            unlockedMilestones = _unlockedMilestones.value
        )
    }

    fun importSaveJson(json: String) {
        val entity = repository.parseImportedSave(json) ?: return
        viewModelScope.launch {
            repository.saveToSlot(
                slotKey = "autosave",
                slotTitle = "Restored City",
                stats = CityStats(
                    cityName = entity.cityName,
                    mayorName = entity.mayorName,
                    funds = entity.funds,
                    population = entity.population,
                    happiness = entity.happiness,
                    day = entity.dayCount,
                    timeOfDay = entity.timeOfDay,
                    taxRate = entity.taxRate,
                    totalPlaySeconds = entity.totalPlaySeconds
                ),
                grid = GameSerializer.deserializeGrid(entity.gridDataJson),
                unlockedMilestones = GameSerializer.deserializeMilestones(entity.unlockedMilestonesJson)
            )
            loadFromEntity(entity)
        }
    }

    fun startNewCity(cityName: String, mayorName: String, preset: String) {
        val initialGrid = GameSerializer.generateDefaultGrid(size = 12, preset = preset)
        _grid.value = initialGrid
        _stats.value = CityStats(
            cityName = cityName,
            mayorName = mayorName,
            funds = 3000L,
            population = 0,
            happiness = 85,
            day = 1,
            timeOfDay = 9.0f,
            taxRate = 9
        )
        _unlockedMilestones.value = emptySet()
        _selectedTile.value = null
        _activeBuildingTool.value = null
        _isBulldozeMode.value = false

        recalculateSimulation()
        triggerAutoSave()
    }

    private fun loadFromEntity(entity: SaveGameEntity) {
        val loadedGrid = GameSerializer.deserializeGrid(entity.gridDataJson)
        val loadedMilestones = GameSerializer.deserializeMilestones(entity.unlockedMilestonesJson)

        _grid.value = loadedGrid
        _unlockedMilestones.value = loadedMilestones
        _stats.value = CityStats(
            cityName = entity.cityName,
            mayorName = entity.mayorName,
            funds = entity.funds,
            population = entity.population,
            happiness = entity.happiness,
            day = entity.dayCount,
            timeOfDay = entity.timeOfDay,
            taxRate = entity.taxRate,
            totalPlaySeconds = entity.totalPlaySeconds
        )
        _selectedTile.value = null
        _activeBuildingTool.value = null
        _isBulldozeMode.value = false

        recalculateSimulation()
    }
}
