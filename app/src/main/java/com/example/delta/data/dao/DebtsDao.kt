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

    @Query("""
    SELECT * FROM debts 
    WHERE unitId = :unitId 
    AND SUBSTR(due_date, 1, 4) = :yearStr 
    AND SUBSTR(due_date, 6, 2) = :monthStr
""")
    fun getDebtsForUnitAndMonth(unitId: Long, yearStr: String, monthStr: String): List<Debts>


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
      AND unitId = :unitId
""")
    suspend fun getDebtsForUnitCostCurrentAndPreviousUnpaid(buildingId: Long, costId: Long, unitId: Long): List<Debts>


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
