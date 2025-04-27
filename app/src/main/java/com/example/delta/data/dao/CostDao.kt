package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Units
import kotlinx.coroutines.flow.Flow

@Dao
interface CostDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCost(costs: Costs): Long

    @Delete()
    suspend fun deleteCost(costs: Costs)

    @Query("SELECT * FROM costs")
    fun getAllCost(): Flow<List<Costs>>

    @Query("SELECT * FROM costs where id = :costId")
    fun getCost(costId: Long): Costs


    @Query(
        """
    SELECT DISTINCT units.*
    FROM units
    INNER JOIN debts ON units.unitId = debts.unitId
    WHERE debts.costId = :costId
      AND debts.buildingId = :buildingId
      AND units.buildingId = :buildingId
"""
    )
    fun getUnitsOfBuildingFromCost(
        costId: Long,
        buildingId: Long
    ): List<Units>


    @Query("SELECT * FROM costs where buildingId = 0")
    fun getAllMenuCost(): Flow<List<Costs>>

    @Query("SELECT * FROM costs WHERE buildingId = :buildingId")
    fun getFlowCostsForBuilding(buildingId: Long): Flow<List<Costs>>

    @Query("SELECT MAX(id) FROM costs")
    fun getLastCostId(): Long

    @Query("SELECT * FROM costs")
    suspend fun getCosts(): List<Costs>

    @Query("SELECT * FROM costs where buildingId = :buildingId")
    suspend fun getCostsForBuilding(buildingId: Long): List<Costs>

    @Query("SELECT * FROM costs WHERE buildingId IS NULL")
    suspend fun getCostsWithNullBuildingId(): List<Costs>


    @Query("DELETE FROM costs WHERE buildingId = :buildingId")
    suspend fun deleteCostsForBuilding(buildingId: Long)
}
