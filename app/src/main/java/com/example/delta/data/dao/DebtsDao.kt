package com.example.delta.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.delta.data.entity.Debts
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: Debts)

    @Query("SELECT * FROM debts WHERE unitId = :unitId and payment_flag = 0")
    fun getDebtsForUnit(unitId: Long): Flow<List<Debts>>

    @Query("SELECT * FROM debts WHERE unitId = :unitId and payment_flag = 1")
    fun getPaysForUnit(unitId: Long): Flow<List<Debts>>

    @Query("SELECT * FROM debts")
    fun getAllDebts(): Flow<List<Debts>>

    @Update
    suspend fun updateDebt(debt: Debts)

    @Delete()
    suspend fun deleteDebt(debt: Debts)
}
