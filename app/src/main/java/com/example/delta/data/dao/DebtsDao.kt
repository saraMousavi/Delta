package com.example.delta.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtsDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDebt(debt: Debts) : Long


    @Query("SELECT * FROM debts WHERE unitId = :unitId and costId = :costId and payment_flag = 0")
    suspend fun getDebtsForUnit(unitId: Long, costId: Long): Debts?

    //@TODO filter current date

    @Query("SELECT * FROM debts WHERE unitId = :unitId ORDER BY due_date ASC")
    suspend fun getDebtsOneUnit(unitId: Long): List<Debts>

    @Query("SELECT * FROM debts WHERE unitId = :unitId and payment_flag = 1")
    fun getPaysForUnit(unitId: Long): Flow<List<Debts>>

    @Query("SELECT * FROM debts")
    suspend fun getAllDebts(): List<Debts>

    @Update
    suspend fun updateDebt(debt: Debts)


    @Delete()
    suspend fun deleteDebt(debt: Debts)

    @Query("DELETE FROM debts WHERE costId IN (SELECT id FROM costs WHERE buildingId = :buildingId)")
    suspend fun deleteDebtsForBuilding(buildingId: Long)
}
