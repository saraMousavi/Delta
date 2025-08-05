package com.example.delta.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.delta.data.entity.Credits
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditsDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCredit(debt: Credits) : Long


    @Query("""
    SELECT credits.*
    FROM credits
""")
    fun getCredits(): List<Credits>

    @Query(
        "SELECT * FROM credits " +
            "WHERE buildingId = :buildingId and receipt_flag = 0 ORDER BY due_date ASC")
    suspend fun getCreditsForBuilding(buildingId: Long): List<Credits>

    @Query(
        "SELECT * FROM credits " +
            "WHERE buildingId = :buildingId and receipt_flag = 1  ORDER BY due_date ASC")
    suspend fun getReceiptForBuilding(buildingId: Long): List<Credits>

    @Query("SELECT * FROM credits where creditsId =:creditId")
    suspend fun getCredit(creditId: Long): Credits?

    @Query("SELECT * FROM credits c " +
            "where c.earningsId = :earningsId")
    fun getCreditFromEarning(earningsId: Long): Flow<List<Credits>>

    @Query("SELECT * FROM credits WHERE earningsId = :earningsId")
    suspend fun getCreditsFromEarning(earningsId: Long): List<Credits>


    @Update
    suspend fun updateCredit(debt: Credits)


    @Delete()
    suspend fun deleteCredit(debt: Credits)

    @Query("DELETE FROM credits WHERE  buildingId = :buildingId")
    suspend fun deleteCreditsForBuilding(buildingId: Long)


        // Sum of credits.amount where cost.fundFlag = +1 and credits.paymentFlag = 1 for given building
    //@todo AND c.fund_flag = 1
        @Query(
            """
        SELECT SUM(d.amount) FROM credits d
        INNER JOIN earnings e ON d.earningsId = e.earningsId
        WHERE d.buildingId = :buildingId 
          AND d.receipt_flag = 1
    """
        )
        fun sumReceiptFundFlagPositive(buildingId: Long): Flow<Double>

        // Sum of credits.amount where cost.fundFlag = -1 and credits.paymentFlag = 0 for given building
        //@todo AND c.fund_flag = -1
        @Query(
            """
        SELECT SUM(d.amount) FROM credits d
        INNER JOIN earnings e ON d.earningsId = e.earningsId
        WHERE d.buildingId = :buildingId 
          
          AND d.receipt_flag = 0
    """
        )
        fun sumUnReceiptFundFlagNegative(buildingId: Long): Flow<Double>

    @Query(
        """
        SELECT SUM(d.amount) FROM credits d
        INNER JOIN earnings e ON d.earningsId = e.earningsId
        WHERE d.buildingId = :buildingId 
    """
    )
    suspend fun sumAllCreditAmount(buildingId: Long): Double

    @Query("""
    SELECT COUNT(*) FROM credits 
    WHERE buildingId = :buildingId
      AND description = :earningsName
      AND (
        (due_date BETWEEN :startDate AND :endDate) OR
        (:startDate BETWEEN due_date AND :endDate) OR 
        (:endDate BETWEEN due_date AND :startDate)
      )
      AND receipt_flag = 0
""")
    suspend fun countConflictingCredits(
        buildingId: Long,
        earningsName: String,
        startDate: String,
        endDate: String
    ): Int

    @Query("UPDATE credits SET receipt_flag = :receiptFlag WHERE creditsId IN (:creditIds)")
    suspend fun updateReceiptFlagByIds(creditIds: List<Long>, receiptFlag: Boolean)

}
