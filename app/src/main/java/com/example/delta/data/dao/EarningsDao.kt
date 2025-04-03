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

    @Query("SELECT * FROM earnings WHERE buildingId = :buildingId")
    fun getEarningsForBuilding(buildingId: Long): Flow<List<Earnings>>
}
