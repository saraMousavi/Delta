package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Earnings
import kotlinx.coroutines.flow.Flow

@Dao
interface EarningsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEarnings(earnings: Earnings) : Long

    @Delete()
    suspend fun deleteEarnings(earnings: Earnings)

    @Query("SELECT * FROM earnings")
    fun getAllEarnings(): Flow<List<Earnings>>


    @Query("SELECT EXISTS(SELECT 1 FROM earnings WHERE buildingId = :buildingId AND earnings_name = :earningName)")
    suspend fun earningNameExists(buildingId: Long, earningName: String): Boolean

    @Query("SELECT * FROM earnings")
    fun getEarnings(): List<Earnings>

    @Query("SELECT b.* FROM earnings e " +
            "inner join buildings b on " +
            "b.buildingId = e.buildingId" +
            " where e.earningsId = :earningsId")
    suspend fun getBuildingFromEarning(earningsId: Long): Buildings?

    @Query("SELECT * FROM earnings where buildingId IS NULL")
    suspend fun getAllMenuEarnings(): List<Earnings>

    @Query("SELECT * FROM earnings WHERE buildingId = :buildingId")
    suspend fun getEarningsForBuilding(buildingId: Long): List<Earnings>

    @Query("SELECT * FROM earnings WHERE earningsId = :earningsId")
    fun getEarning(earningsId: Long): Flow<Earnings?>

    @Query("SELECT * FROM earnings WHERE buildingId = :buildingId")
    fun getFlowEarningsForBuilding(buildingId: Long): Flow<List<Earnings>>

    // Sum of debts.amount where cost.fundFlag = -1 and debts.paymentFlag = 0 for given building
    @Query("""
        SELECT SUM(amount) FROM earnings where buildingId = :buildingId
    """)
    fun sumPaidEarning(buildingId: Long): Flow<Double>

    @Query("""
    SELECT * FROM earnings e
    WHERE e.buildingId = :buildingId
    AND EXISTS (
        SELECT 1 FROM credits c
        WHERE c.earningsId = e.earningsId
          AND c.receipt_flag = 0
    )
    ORDER BY e.start_date ASC
""")
    fun getNotInvoicedEarnings(buildingId: Long): Flow<List<Earnings>>

}
