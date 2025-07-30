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
    suspend fun getEarningsForBuilding(buildingId: Long): List<Earnings>

    @Query("SELECT * FROM earnings WHERE buildingId = :buildingId")
    fun getFlowEarningsForBuilding(buildingId: Long): Flow<List<Earnings>>

    // Sum of debts.amount where cost.fundFlag = -1 and debts.paymentFlag = 0 for given building
    @Query("""
        SELECT SUM(amount) FROM earnings where buildingId = :buildingId
    """)
    fun sumPaidEarning(buildingId: Long): Flow<Double>

    @Query("""
        SELECT * FROM earnings
        WHERE buildingId = :buildingId
        AND (invoice_flag = 0 OR invoice_flag IS NULL)
        ORDER BY due_date ASC
    """)
    fun getNotInvoicedEarnings(buildingId: Long): Flow<List<Earnings>>

    @Query("""
        UPDATE earnings SET invoice_flag = 1 WHERE earningsId = :earningId
    """)
    suspend fun markEarningAsInvoiced(earningId: Long)
}
