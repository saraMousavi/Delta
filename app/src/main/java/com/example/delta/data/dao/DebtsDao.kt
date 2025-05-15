package com.example.delta.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.delta.data.entity.Debts
import com.example.delta.enums.Responsible

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
    INNER JOIN costs ON debts.costId = costs.id
    WHERE debts.unitId = :unitId
      AND SUBSTR(debts.due_date, 1, 4) = :yearStr
      AND SUBSTR(debts.due_date, 6, 2) = :monthStr
      AND debts.payment_flag = 0
      AND costs.responsible = :responsible
""")
    fun getDebtsForUnits(unitId: Long, yearStr: String, monthStr: String, responsible: Responsible): List<Debts>

    @Query("""
    SELECT debts.*
    FROM debts
    INNER JOIN costs ON debts.costId = costs.id
    INNER JOIN owners_units_cross_ref ON debts.unitId = owners_units_cross_ref.unitId
    WHERE owners_units_cross_ref.ownerId = :ownerId 
    AND SUBSTR(due_date, 1, 4) = :yearStr 
    AND SUBSTR(due_date, 6, 2) = :monthStr
    AND payment_flag = 0
    AND costs.responsible = :responsible
""")
    fun getDebtsForOwner(ownerId: Long, yearStr: String, monthStr: String, responsible: Responsible): List<Debts>

    @Query("""
    SELECT debts.*
    FROM debts
    INNER JOIN owners_units_cross_ref ON debts.unitId = owners_units_cross_ref.unitId
    inner join costs on debts.costId = costs.id
    WHERE owners_units_cross_ref.ownerId = :ownerId 
    AND payment_flag = 1
    AND costs.responsible = :responsible
    ORDER BY due_date ASC
""")
    fun getPaysForOwner(ownerId: Long, responsible: Responsible): List<Debts>



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
        suspend fun sumPaidFundFlagPositive(buildingId: Long): Double

        // Sum of debts.amount where cost.fundFlag = -1 and debts.paymentFlag = 0 for given building
        @Query("""
        SELECT SUM(d.amount) FROM debts d
        INNER JOIN costs c ON d.costId = c.id
        WHERE d.buildingId = :buildingId 
          AND c.fund_flag = -1
          AND d.payment_flag = 0
    """)
        suspend fun sumUnpaidFundFlagNegative(buildingId: Long): Double

}
