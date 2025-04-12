package com.example.delta.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.delta.data.entity.Units

@Dao
interface UnitsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: Units): Long

    @Update
    suspend fun updateUnit(unit: Units)

    @Delete
    suspend fun deleteUnit(unit: Units)

    @Query("SELECT * FROM units WHERE buildingId = :buildingId")
    suspend fun getUnitsByBuildingId(buildingId: Long): List<Units>


}