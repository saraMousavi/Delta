package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Units
import com.example.delta.enums.FundFlag
import com.example.delta.enums.PaymentLevel
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

    @Query("SELECT * FROM costs WHERE buildingId = :buildingId  AND fund_flag != :fundFlag")
    suspend fun getCostsForBuildingWithFundFlag(buildingId: Long, fundFlag: FundFlag): List<Costs>

    @Query("SELECT * FROM costs WHERE buildingId = :buildingId AND cost_name = :costName LIMIT 1")
    suspend fun getCostByBuildingIdAndName(buildingId: Long, costName: String): Costs?

    @Update
    suspend fun updateCost(cost: Costs): Int

    @Query("SELECT * FROM costs WHERE buildingId IS NULL")
    suspend fun getCostsWithNullBuildingId(): List<Costs>

    @Query("SELECT * FROM costs WHERE id = :costId LIMIT 1")
    suspend fun getCostById(costId: Long): Costs



    @Query("DELETE FROM costs WHERE buildingId = :buildingId")
    suspend fun deleteCostsForBuilding(buildingId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM costs WHERE buildingId = :buildingId AND cost_name = :costName)")
    suspend fun costNameExists(buildingId: Long, costName: String): Boolean

}
