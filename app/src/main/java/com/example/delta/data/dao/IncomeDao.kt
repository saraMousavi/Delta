package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.Income
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: Income)

    @Delete()
    suspend fun deleteIncome(income: Income)

    @Query("SELECT * FROM incomes")
    fun getAllIncome(): Flow<List<Income>>

    @Query("SELECT * FROM incomes WHERE buildingId = :buildingId")
    fun getIncomesForBuilding(buildingId: Long): Flow<List<Income>>
}
