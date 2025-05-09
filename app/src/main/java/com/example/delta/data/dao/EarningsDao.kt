package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.Earnings
import kotlinx.coroutines.flow.Flow

@Dao
interface EarningsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEarnings(earnings: Earnings)

    @Delete()
    suspend fun deleteEarnings(earnings: Earnings)

    @Query("SELECT * FROM earnings")
    fun getAllEarnings(): Flow<List<Earnings>>

    @Query("SELECT * FROM earnings")
    fun getEarnings(): List<Earnings>

    @Query("SELECT * FROM earnings where buildingId IS NULL")
    suspend fun getAllMenuEarnings(): List<Earnings>

    @Query("SELECT * FROM earnings WHERE buildingId = :buildingId")
    fun getEarningsForBuilding(buildingId: Long): Flow<List<Earnings>>

    // Sum of debts.amount where cost.fundFlag = -1 and debts.paymentFlag = 0 for given building
    @Query("""
        SELECT SUM(amount) FROM earnings where buildingId = :buildingId
    """)
    suspend fun sumPaidEarning(buildingId: Long): Double
}
