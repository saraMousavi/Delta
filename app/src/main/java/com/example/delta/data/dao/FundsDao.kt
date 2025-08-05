package com.example.delta.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.delta.data.entity.Funds
import com.example.delta.enums.FundType
import kotlinx.coroutines.flow.Flow

@Dao
interface FundsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFunds(funds: Funds)

    @Delete()
    suspend fun deleteFunds(funds: Funds)

    @Query("SELECT * FROM funds")
    suspend fun getAllFunds(): List<Funds>


    @Update
    suspend fun updateFunds(funds: Funds)


    @Query("select balance from funds where buildingId =:buildingId and fund_type = :fundType")
    fun getOperationalOrCapitalFundBalance(buildingId: Long, fundType: FundType): Flow<Double>


    @Query("SELECT * FROM funds WHERE buildingId = :buildingId AND fund_type = :fundType LIMIT 1")
    suspend fun getFundByType(buildingId: Long, fundType: FundType): Funds?

}