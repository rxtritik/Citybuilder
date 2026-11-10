package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "save_slots")
data class SaveGameEntity(
    @PrimaryKey
    val slotKey: String, // e.g. "autosave", "slot_1", "slot_2", "slot_3"
    val slotTitle: String,
    val cityName: String,
    val mayorName: String,
    val funds: Long,
    val population: Int,
    val happiness: Int,
    val dayCount: Int,
    val timeOfDay: Float,
    val taxRate: Int,
    val gridDataJson: String,
    val unlockedMilestonesJson: String,
    val totalPlaySeconds: Long,
    val lastSavedTimestamp: Long = System.currentTimeMillis()
)
