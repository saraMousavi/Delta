package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.Cost
import kotlinx.coroutines.flow.Flow

@Dao
interface CostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCost(cost: Cost)

    @Delete()
    suspend fun deleteCost(cost: Cost)

    @Query("SELECT * FROM costs")
    fun getAllCost(): Flow<List<Cost>>

    @Query("SELECT * FROM costs WHERE buildingId = :buildingId")
    fun getCostsForBuilding(buildingId: Long): Flow<List<Cost>>
}
