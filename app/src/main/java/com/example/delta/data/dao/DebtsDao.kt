package com.example.delta.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.DebtWithUnitName
import com.example.delta.data.entity.Debts
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtsDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDebt(debt: Debts) : Long


    @Query("SELECT * FROM debts WHERE unitId = :unitId and costId = :costId and payment_flag = 0")
    suspend fun getDebtsForUnit(unitId: Long, costId: Long): Debts?

    //@TODO filter current date

    @Query("SELECT * FROM debts WHERE unitId = :unitId and payment_flag = 0 ORDER BY due_date ASC")
    suspend fun getDebtsOneUnit(unitId: Long): List<Debts>

    @Query("SELECT * FROM debts WHERE buildingId = :buildingId and costId = :costId ORDER BY due_date ASC")
    suspend fun getDebtsOfCosts(buildingId: Long, costId: Long): List<Debts>

//    @Query("SELECT * FROM debts WHERE buildingId = :buildingId and costId = :costId and unitId = :unitId ORDER BY due_date ASC")
//    suspend fun getDebtsOfCostsForUnit(buildingId: Long, costId: Long, unitId: Long): List<Debts>

//    @Query("""
//    SELECT debts.*, units.unit_number
//    FROM debts
//    INNER JOIN units ON debts.unitId = units.unitId
//    WHERE debts.buildingId = :buildingId
//      AND debts.costId = :costId
//      AND debts.unitId = :unitId
//
//    ORDER BY debts.due_date ASC
//""")
//    suspend fun getDebtsWithUnitNameForUnitCostCurrentAndPreviousUnpaid(
//        buildingId: Long,
//        costId: Long,
//        unitId: Long
////        ,        currentYearMonth: String
//    ): List<DebtWithUnitName>
//@Query("""
//    SELECT debts.*
//    FROM debts
//    WHERE debts.buildingId = :buildingId
//
//    ORDER BY debts.due_date ASC
//""")
//suspend fun getDebtsWithUnitNameForUnitCostCurrentAndPreviousUnpaid(
//    buildingId: Long
////        ,        currentYearMonth: String
//): List<Debts>

//    AND (
//    (debts.due_date LIKE :currentYearMonth || '%')
//    OR
//    (debts.due_date < :currentYearMonth AND debts.payment_flag = 0)
//    )

    @Query("""
    SELECT * FROM debts
    WHERE buildingId = :buildingId
      AND costId = :costId
      AND unitId = :unitId
""")
    suspend fun getDebtsForUnitCostBuilding(buildingId: Long, costId: Long, unitId: Long): List<Debts>


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
}
