package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BuildToolBar
import com.example.ui.components.DownloadApkDialog
import com.example.ui.components.InspectDialog
import com.example.ui.components.MilestoneDialog
import com.example.ui.components.NewCityDialog
import com.example.ui.components.SaveLoadModal
import com.example.ui.components.TopBarStats

@Composable
fun CityBuilderScreen(
    viewModel: CityGameViewModel,
    modifier: Modifier = Modifier
) {
    val grid by viewModel.grid.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val unlockedMilestones by viewModel.unlockedMilestones.collectAsStateWithLifecycle()
    val selectedTile by viewModel.selectedTile.collectAsStateWithLifecycle()
    val activeBuildingTool by viewModel.activeBuildingTool.collectAsStateWithLifecycle()
    val isBulldozeMode by viewModel.isBulldozeMode.collectAsStateWithLifecycle()
    val isAutoSavedRecent by viewModel.isAutoSavedRecent.collectAsStateWithLifecycle()
    val saveSlots by viewModel.saveSlots.collectAsStateWithLifecycle()

    var showSaveLoadDialog by remember { mutableStateOf(false) }
    var showMilestonesDialog by remember { mutableStateOf(false) }
    var showNewCityDialog by remember { mutableStateOf(false) }
    var showDownloadApkDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive 2D/Isometric Canvas Grid
            CityGridView(
                grid = grid,
                selectedTile = selectedTile,
                activeBuildingTool = activeBuildingTool,
                isBulldozeMode = isBulldozeMode,
                timeOfDay = stats.timeOfDay,
                onTileClicked = { x, y -> viewModel.onTileClicked(x, y) },
                modifier = Modifier.fillMaxSize()
            )

            // Top Bar HUD
            TopBarStats(
                stats = stats,
                isAutoSavedRecent = isAutoSavedRecent,
                onSpeedChange = { speed -> viewModel.setSimulationSpeed(speed) },
                onOpenSaveLoad = { showSaveLoadDialog = true },
                onOpenMilestones = { showMilestonesDialog = true },
                onOpenNewCity = { showNewCityDialog = true },
                onOpenDownloadApk = { showDownloadApkDialog = true },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
            )

            // Bottom Build Toolbar Dock
            BuildToolBar(
                currentFunds = stats.funds,
                population = stats.population,
                selectedBuilding = activeBuildingTool,
                isBulldozeMode = isBulldozeMode,
                onSelectBuilding = { building -> viewModel.selectBuildingTool(building) },
                onToggleBulldoze = { viewModel.toggleBulldozeMode() },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Inspect Tile Dialog
            if (selectedTile != null) {
                val (sx, sy) = selectedTile!!
                if (sy in grid.indices && sx in grid[sy].indices) {
                    val tile = grid[sy][sx]
                    InspectDialog(
                        tile = tile,
                        currentFunds = stats.funds,
                        onUpgrade = { viewModel.upgradeSelectedTile() },
                        onDemolish = { viewModel.demolishSelectedTile() },
                        onDismiss = { viewModel.clearSelection() }
                    )
                }
            }

            // Save & Load Modal
            if (showSaveLoadDialog) {
                SaveLoadModal(
                    saveSlots = saveSlots,
                    onSaveToSlot = { slotKey, title -> viewModel.saveToSlot(slotKey, title) },
                    onLoadSlot = { slotKey ->
                        viewModel.loadSlot(slotKey)
                        showSaveLoadDialog = false
                    },
                    onDeleteSlot = { slotKey -> viewModel.deleteSlot(slotKey) },
                    onExportJson = { viewModel.exportSaveJson() },
                    onImportJson = { json -> viewModel.importSaveJson(json) },
                    onDismiss = { showSaveLoadDialog = false }
                )
            }

            // Milestones Dialog
            if (showMilestonesDialog) {
                MilestoneDialog(
                    currentPopulation = stats.population,
                    unlockedMilestones = unlockedMilestones,
                    onClaimMilestone = { milestone -> viewModel.claimMilestoneReward(milestone) },
                    onDismiss = { showMilestonesDialog = false }
                )
            }

            // New City Setup Dialog
            if (showNewCityDialog) {
                NewCityDialog(
                    initialCityName = stats.cityName,
                    initialMayorName = stats.mayorName,
                    onConfirm = { cityName, mayorName, preset ->
                        viewModel.startNewCity(cityName, mayorName, preset)
                    },
                    onDismiss = { showNewCityDialog = false }
                )
            }

            // Download APK Info Dialog
            if (showDownloadApkDialog) {
                DownloadApkDialog(
                    onDismiss = { showDownloadApkDialog = false }
                )
            }
        }
    }
}
