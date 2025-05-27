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
      AND SUBSTR(debts.due_date, 1, 4) = :yearStr
      AND SUBSTR(debts.due_date, 6, 2) = :monthStr
      AND debts.payment_flag = 0
""")
    fun getDebtsForUnits(unitId: Long, yearStr: String, monthStr: String): List<Debts>

    @Query("""
    SELECT debts.*
    FROM debts
    WHERE debts.ownerId = :ownerId 
    AND SUBSTR(due_date, 1, 4) = :yearStr 
    AND SUBSTR(due_date, 6, 2) = :monthStr
    AND payment_flag = 0
""")
    fun getDebtsForOwner(ownerId: Long, yearStr: String, monthStr: String): List<Debts>

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


    @Query("SELECT * FROM debts inner join costs on costs.id = debts.costId " +
            "WHERE costs.buildingId = :buildingId ORDER BY due_date ASC")
    suspend fun getDebtsOfBuilding(buildingId: Long): List<Debts>

    @Query("SELECT * FROM debts WHERE unitId = :unitId and payment_flag = 1")
    fun getPaysForUnit(unitId: Long): List<Debts>

    @Query("SELECT * FROM debts")
    suspend fun getAllDebts(): List<Debts>

    @Update
    suspend fun updateDebt(debt: Debts)


    @Delete()
    suspend fun deleteDebt(debt: Debts)

    @Query("DELETE FROM debts WHERE  buildingId = :buildingId")
    suspend fun deleteDebtsForBuilding(buildingId: Long)


        // Sum of debts.amount where cost.fundFlag = +1 and debts.paymentFlag = 1 for given building
        @Query("""
        SELECT SUM(d.amount) FROM debts d
        INNER JOIN costs c ON d.costId = c.id
        WHERE d.buildingId = :buildingId 
          AND c.fund_flag = 1
          AND d.payment_flag = 1
    """)
        fun sumPaidFundFlagPositive(buildingId: Long): Flow<Double>

        // Sum of debts.amount where cost.fundFlag = -1 and debts.paymentFlag = 0 for given building
        @Query("""
        SELECT SUM(d.amount) FROM debts d
        INNER JOIN costs c ON d.costId = c.id
        WHERE d.buildingId = :buildingId 
          AND c.fund_flag = -1
          AND d.payment_flag = 0
    """)
        fun sumUnpaidFundFlagNegative(buildingId: Long): Flow<Double>

    // Sum of debts.amount where cost.fundFlag = -1 and debts.paymentFlag = 0 for given building
    @Query("""
        SELECT SUM(d.amount) FROM debts d
        INNER JOIN costs c ON d.costId = c.id
        WHERE d.buildingId = :buildingId 
          AND c.responsible = :responsible
    """)
    fun sumFundMinus(buildingId: Long, responsible:Responsible): Flow<Double>

}
