package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.Costs
import kotlinx.coroutines.flow.Flow

@Dao
interface CostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCost(costs: Costs)

    @Delete()
    suspend fun deleteCost(costs: Costs)

    @Query("SELECT * FROM costs")
    fun getAllCost(): Flow<List<Costs>>

    @Query("SELECT * FROM costs where id = :costId")
    fun getCost(costId: Long): Costs


    @Query("SELECT * FROM costs where buildingId = 0")
    fun getAllMenuCost(): Flow<List<Costs>>

    @Query("SELECT * FROM costs WHERE buildingId = :buildingId")
    fun getCostsForBuilding(buildingId: Long): Flow<List<Costs>>

    @Query("SELECT MAX(id) FROM costs")
    fun getLastCostId(): Long
}
