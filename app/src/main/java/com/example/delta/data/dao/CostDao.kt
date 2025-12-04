package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Units
import com.example.delta.enums.FundType
import kotlinx.coroutines.flow.Flow

@Dao
interface CostDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCost(costs: Costs): Long

    @Delete()
    suspend fun deleteCost(costs: Costs)

    @Query("SELECT * FROM costs")
    fun getAllCost(): Flow<List<Costs>>

    @Query("SELECT * FROM costs where costId = :costId")
    fun getCost(costId: Long): Costs


    @Query(
        """
    SELECT DISTINCT units.*
    FROM units
    INNER JOIN debts ON units.unitId = debts.unitId
    WHERE debts.costId = :costId
      AND debts.buildingId = :buildingId
"""
    )
    fun getUnitsOfBuildingFromCost(
        costId: Long,
        buildingId: Long
    ): List<Units>



    @Query("SELECT * FROM costs where buildingId is null")
    fun getAllMenuCost(): Flow<List<Costs>>

    @Query("SELECT * FROM costs WHERE buildingId = :buildingId")
    fun getFlowCostsForBuilding(buildingId: Long): Flow<List<Costs>>

    @Query("SELECT MAX(costId) FROM costs")
    fun getLastCostId(): Long

    @Query("SELECT * FROM costs")
    suspend fun getCosts(): List<Costs>



    @Query("SELECT c.cost_name FROM costs c where charge_flag = 1")
    suspend fun getCostsOfCharges(): List<String>

    @Query(
        "SELECT * FROM costs WHERE buildingId = :buildingId AND fund_type != :fundType " +
                "and cost_name != 'شارژ'"
    )
    suspend fun getCostsByFundTypeForBuilding(buildingId: Long, fundType: FundType): List<Costs>



    @Query("SELECT * FROM costs WHERE buildingId = :buildingId  AND charge_flag = 1")
    suspend fun getCostsForBuildingWithChargeFlag(buildingId: Long): List<Costs>

    @Query("""
    SELECT * FROM costs
    WHERE buildingId = :buildingId
    AND charge_flag = 1
    AND due_date LIKE (:fiscalYear || '%')
""")
    fun getCostsForBuildingWithChargeFlagAndFiscalYear(buildingId: Long, fiscalYear: String): Flow<List<Costs>>


    @Query("""
    SELECT * FROM costs
    WHERE buildingId = :buildingId
    AND charge_flag = 1
    AND cost_name = :costName
    AND due_date LIKE (:fiscalYear || '%')
    LIMIT 1
""")
    suspend fun getCostsForBuildingWithChargeFlagAndFiscalYearAndCostName(buildingId: Long, fiscalYear: String, costName : String): Costs?

    @Query("""
    SELECT * FROM costs
    WHERE buildingId = :buildingId
    AND charge_flag = 0
    AND cost_name = 'هزینه های عمرانی'
    AND due_date LIKE (:fiscalYear || '%')
    LIMIT 1
""")
    suspend fun getCapitalCostsForBuildingWithFiscalYear(buildingId: Long, fiscalYear: String): Costs?

    @Query("""
    SELECT * FROM costs
    WHERE buildingId = :buildingId
    AND charge_flag = 0
    AND cost_name = 'هزینه های عمرانی'
""")
    suspend fun getCapitalCostsForBuilding(buildingId: Long): List<Costs>


    @Query("""
    SELECT * FROM costs c1
    WHERE c1.buildingId IS NULL
    and c1.cost_name NOT IN (
        SELECT c2.cost_name FROM costs c2 WHERE c2.buildingId = :buildingId
    )
    AND c1.charge_flag = 1
""")
    suspend fun getChargesCostsNotInBuilding(buildingId: Long): List<Costs>


    @Query("SELECT * FROM costs WHERE buildingId = :buildingId AND cost_name = :costName LIMIT 1")
    suspend fun getDefaultCostByBuildingIdAndName(buildingId: Long, costName: String= "شارژ"): Costs?

    @Query("SELECT * FROM costs WHERE buildingId = :buildingId AND cost_name = :costName and charge_flag = 0 LIMIT 1")
    suspend fun getCostByBuildingIdAndName(buildingId: Long, costName: String= "شارژ"): Costs?

    @Update
    suspend fun updateCost(cost: Costs): Int

    @Query("SELECT * FROM costs WHERE buildingId IS NULL")
    suspend fun getCostsWithNullBuildingId(): List<Costs>


    @Query("SELECT * FROM costs WHERE buildingId IS NULL and charge_flag = 1")
    suspend fun getChargesCostsWithNullBuildingId(): List<Costs>



    @Query("SELECT * FROM costs WHERE buildingId = :buildingId and charge_flag = 1 and temp_amount = 0.0 and due_date = ''")
    fun getRawChargesCostsWithBuildingId(buildingId: Long): Flow<List<Costs>>

    @Query("SELECT * FROM costs WHERE costId = :costId LIMIT 1")
    suspend fun getCostById(costId: Long): Costs


    @Query("SELECT * FROM costs WHERE costId in ( :costId )")
    suspend fun getCostsByIds(costId: List<Long>): List<Costs>

    @Query("DELETE FROM costs WHERE buildingId = :buildingId")
    suspend fun deleteCostsForBuilding(buildingId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM costs WHERE buildingId = :buildingId AND cost_name = :costName)")
    suspend fun costNameExists(buildingId: Long, costName: String): Boolean

    /**
     * Get costs for a building that are NOT invoiced yet (invoiceFlag == false or null)
     * Optionally filter by fund type if needed (capital or operational).
     */
    @Query("""
        SELECT * FROM costs
        WHERE buildingId = :buildingId
        AND (invoice_flag IS NULL OR invoice_flag = 0)
        AND fund_type = :fundType
        and charge_flag = 0
        and capital_flag = 0
        and cost_name != 'شارژ' 
        and cost_name != 'رهن' 
        and cost_name != 'اجاره' 
        ORDER BY due_date ASC
    """)
    fun getPendingCostsByFundType(buildingId: Long, fundType: FundType): Flow<List<Costs>>


    /**
     * Get costs for a building that ARE invoiced (invoiceFlag == true)
     * Optionally filter by fund type.
     */
    @Query("""
        SELECT * FROM costs
        WHERE buildingId = :buildingId
        AND invoice_flag = 1
        AND fund_type = :fundType
        ORDER BY due_date DESC
    """)
    suspend fun getInvoicedCostsByFundType(buildingId: Long, fundType: FundType): List<Costs>


    /**
     * Mark a specific cost as invoiced (set invoiceFlag = true)
     */
    @Query("""
        UPDATE costs
        SET invoice_flag = 1
        WHERE costId = :costId
    """)
    suspend fun markCostAsInvoiced(costId: Long)

}
