package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SaveGameDao {
    @Query("SELECT * FROM save_slots ORDER BY lastSavedTimestamp DESC")
    fun getAllSaveSlots(): Flow<List<SaveGameEntity>>

    @Query("SELECT * FROM save_slots WHERE slotKey = :slotKey LIMIT 1")
    suspend fun getSaveSlotByKey(slotKey: String): SaveGameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(slot: SaveGameEntity)

    @Query("DELETE FROM save_slots WHERE slotKey = :slotKey")
    suspend fun deleteSlot(slotKey: String)

    @Query("SELECT COUNT(*) FROM save_slots")
    suspend fun getSaveSlotCount(): Int
}
