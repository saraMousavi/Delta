package com.example.delta.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.delta.data.entity.Units
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: Units)

    @Delete()
    suspend fun deleteUnits(units: Units)

    @Query("SELECT * FROM units")
    fun getAllUnits(): Flow<List<Units>>


    @Query("SELECT * FROM units WHERE buildingId = :buildingId")
    fun getUnitsForBuilding(buildingId: Long): Flow<List<Units>>

    @Query("SELECT MAX(unitId) FROM units")
    fun getLastUnitId(): Long
}