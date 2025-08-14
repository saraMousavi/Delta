package com.example.delta.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.delta.data.entity.Debts
import com.example.delta.enums.Responsible
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtsDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDebt(debt: Debts) : Long


    @Query("SELECT * FROM debts WHERE unitId = :unitId and costId = :costId and payment_flag = 0")
    suspend fun getDebtsOfCostForUnit(unitId: Long, costId: Long): Debts?

    //@TODO filter current date

    @Query("SELECT * FROM debts WHERE unitId = :unitId and payment_flag = 0 ORDER BY due_date ASC")
    suspend fun getDebtsOneUnit(unitId: Long): List<Debts>

    @Query("""
    SELECT debts.*
    FROM debts
    WHERE debts.unitId = :unitId
      AND (SUBSTR(due_date, 1, 4) = :yearStr)
      AND (SUBSTR(due_date, 6, 2) = :monthStr)
      AND debts.payment_flag = 0
""")
    fun getDebtsForUnits(unitId: Long, yearStr: String?, monthStr: String?): List<Debts>

    @Query("""
    SELECT debts.*
    FROM debts
    WHERE debts.unitId = :unitId
      AND debts.payment_flag = 0
""")
    fun getAllDebtsForUnits(unitId: Long): List<Debts>


    @Query("""
    SELECT debts.* 
    FROM debts
    WHERE debts.ownerId = :ownerId 
      AND ( SUBSTR(due_date, 1, 4) = :yearStr)
      AND (SUBSTR(due_date, 6, 2) = :monthStr)
      AND payment_flag = 0
""")
    fun getDebtsForOwner(
        ownerId: Long,
        yearStr: String?,
        monthStr: String?
    ): List<Debts>

    @Query("""
    SELECT debts.* 
    FROM debts
    WHERE debts.costId = :costId
""")
    fun getDebtsForEachCost(
        costId: Long
    ): List<Debts>

    @Query("""
    SELECT debts.* 
    FROM debts
    WHERE debts.ownerId = :ownerId 
      AND payment_flag = 0
""")
    fun getAllDebtsForOwner(
        ownerId: Long
    ): List<Debts>



    @Query("""
    SELECT debts.*
    FROM debts
    WHERE debts.ownerId = :ownerId 
    AND payment_flag = 1
    ORDER BY due_date ASC
""")
    fun getPaysForOwner(ownerId: Long): List<Debts>



    @Query("SELECT * FROM debts WHERE buildingId = :buildingId and costId = :costId ORDER BY due_date ASC")
    suspend fun getDebtsOfCosts(buildingId: Long, costId: Long): List<Debts>


    @Query("""
    SELECT * FROM debts
    WHERE buildingId = :buildingId
      AND costId = :costId
      AND unitId = :unitId and (
        (SUBSTR(due_date, 1, 4) = :yearStr AND SUBSTR(due_date, 6, 2) = :monthStr)
        OR
        ( due_date < (:yearStr || '/' || :monthStr || '/01') AND payment_flag = 0) 
        )
    ORDER BY due_date ASC
""")
    fun getDebtsCurrentMonthAndPastUnpaid(buildingId: Long, costId: Long, unitId: Long, yearStr: String, monthStr: String): List<Debts>


    @Query("""
    SELECT * FROM debts
    WHERE buildingId = :buildingId
      AND costId = :costId
      and (
        (SUBSTR(due_date, 1, 4) = :yearStr AND SUBSTR(due_date, 6, 2) = :monthStr)
        OR
        ( due_date < (:yearStr || '/' || :monthStr || '/01') AND payment_flag = 0) 
        )
    ORDER BY due_date ASC
""")
    fun getDebtsFundMinus(buildingId: Long, costId: Long, yearStr: String, monthStr: String): List<Debts>


    @Query("""
    SELECT * FROM debts
    WHERE buildingId = :buildingId
      AND costId = :costId
      AND ownerId = :ownerId and (
        (SUBSTR(due_date, 1, 4) = :yearStr AND SUBSTR(due_date, 6, 2) = :monthStr)
        OR
        ( due_date < (:yearStr || '/' || :monthStr || '/01') AND payment_flag = 0) 
        )
    ORDER BY due_date ASC
""")
    fun getDebtsForOwnerCostCurrentAndPreviousUnpaid(buildingId: Long, costId: Long, ownerId: Long, yearStr: String, monthStr: String): List<Debts>


//    @Query("""
//    SELECT * FROM debts
//    WHERE buildingId = :buildingId
//      AND costId = :costId
//      AND unitId = :unitId
//""")
//    suspend fun getDebtsForUnitCostCurrentAndPreviousUnpaid(buildingId: Long, costId: Long, unitId: Long): List<Debts>


    @Query(
        "SELECT * FROM debts inner join costs on costs.costId = debts.costId " +
            "WHERE costs.buildingId = :buildingId and payment_flag = 0 ORDER BY due_date ASC")
    suspend fun getDebtsForBuilding(buildingId: Long): List<Debts>

    @Query(
        "SELECT * FROM debts inner join costs on costs.costId = debts.costId " +
            "WHERE costs.buildingId = :buildingId and payment_flag = 1  ORDER BY due_date ASC")
    suspend fun getPaysForBuilding(buildingId: Long): List<Debts>

    data class CostAmountSummary(
        val costName: String,
        val totalAmount: Double
    )

    @Query("""
    SELECT costs.cost_name AS costName, SUM(debts.amount) AS totalAmount
    FROM debts 
    INNER JOIN costs ON costs.costId = debts.costId
    WHERE costs.buildingId = :buildingId 
      AND payment_flag = 0
    GROUP BY costs.cost_name
    ORDER BY costs.cost_name
""")
    suspend fun getDebtsGroupedByCostName(buildingId: Long): List<CostAmountSummary>

    @Query("""
    SELECT costs.cost_name AS costName, SUM(debts.amount) AS totalAmount
    FROM debts 
    INNER JOIN costs ON costs.costId = debts.costId
    WHERE costs.buildingId = :buildingId 
      AND payment_flag = 1
    GROUP BY costs.cost_name
    ORDER BY costs.cost_name
""")
    suspend fun getPaysGroupedByCostName(buildingId: Long): List<CostAmountSummary>


    @Query("SELECT * FROM debts WHERE unitId = :unitId and payment_flag = 1")
    fun getPaysForUnit(unitId: Long): List<Debts>

    @Query("SELECT * FROM debts")
    suspend fun getAllDebts(): List<Debts>


    @Query("SELECT * FROM debts where debtId =:debtId")
    suspend fun getDebt(debtId: Long): Debts?

    @Update
    suspend fun updateDebt(debt: Debts)


    @Delete()
    suspend fun deleteDebt(debt: Debts)

    @Query("DELETE FROM debts WHERE  buildingId = :buildingId")
    suspend fun deleteDebtsForBuilding(buildingId: Long)


        // Sum of debts.amount where cost.fundFlag = +1 and debts.paymentFlag = 1 for given building
    //@todo AND c.fund_flag = 1
        @Query(
            """
        SELECT SUM(d.amount) FROM debts d
        INNER JOIN costs c ON d.costId = c.costId
        WHERE d.buildingId = :buildingId 
          AND d.payment_flag = 1
    """
        )
        fun sumPaidFundFlagPositive(buildingId: Long): Flow<Double>

        // Sum of debts.amount where cost.fundFlag = -1 and debts.paymentFlag = 0 for given building
        //@todo AND c.fund_flag = -1
        @Query(
            """
        SELECT SUM(d.amount) FROM debts d
        INNER JOIN costs c ON d.costId = c.costId
        WHERE d.buildingId = :buildingId 
          
          AND d.payment_flag = 0
    """
        )
        fun sumUnpaidFundFlagNegative(buildingId: Long): Flow<Double>

    // Sum of debts.amount where cost.fundFlag = -1 and debts.paymentFlag = 0 for given building
    @Query(
        """
        SELECT SUM(d.amount) FROM debts d
        INNER JOIN costs c ON d.costId = c.costId
        WHERE d.buildingId = :buildingId 
          AND c.responsible = :responsible
    """
    )
    fun sumFundMinus(buildingId: Long, responsible:Responsible): Flow<Double>

    @Query("""
        SELECT d.unitId, SUM(d.amount) / 12 as totalAmount
        FROM debts d
        INNER JOIN costs c ON d.costId = c.costId
        WHERE d.buildingId = :buildingId 
          AND c.cost_name = 'شارژ'
          AND d.due_date LIKE :fiscalYear || '%'
        GROUP BY d.unitId
    """)
    suspend fun getTotalChargesByUnitForChargeCost(
        buildingId: Long,
        fiscalYear: String
    ): List<UnitChargeAggregate>

    @Query("""
    SELECT * FROM debts 
    WHERE buildingId = :buildingId AND costId = :costId AND unitId = :unitId 
      AND due_date = :dueDate 
      AND (ownerId = :ownerId OR (:ownerId IS NULL AND ownerId IS NULL))
    LIMIT 1
""")
    suspend fun getDebtByKeys(
        buildingId: Long,
        costId: Long,
        unitId: Long?,
        dueDate: String,
        ownerId: Long?
    ): Debts?

    @Query("""
        SELECT debts.* FROM debts
        INNER JOIN tenants_units_cross_ref AS crossRef ON debts.unitId = crossRef.unitId
        WHERE debts.unitId = :unitId
          AND debts.costId = :costId
          AND crossRef.tenantId = :tenantId
        LIMIT 1
    """)
    suspend fun getDebtForTenantAndCost(
        unitId: Long,
        tenantId: Long,
        costId: Long
    ): Debts?

    @Query("""
        SELECT * FROM debts 
        WHERE unitId = :unitId AND costId = :costId AND due_date = :dueDate LIMIT 1
    """)
    suspend fun getDebtForUnitCostAndDueDate(
        unitId: Long,
        costId: Long,
        dueDate: String
    ): Debts?

    @Query("""
        SELECT * FROM debts 
        WHERE ownerId = :ownerId AND payment_flag = 0 and description = 'شارژ' LIMIT 1
    """)
    suspend fun getChargeDebtsForOwners(
        ownerId: Long
    ): List<Debts>


    data class UnitChargeAggregate(
        val unitId: Long?,
        val totalAmount: Double
    )


}
