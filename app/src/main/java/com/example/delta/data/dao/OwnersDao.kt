package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.Owners
import kotlinx.coroutines.flow.Flow

@Dao
interface OwnersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwners(owners: Owners)

    @Delete()
    suspend fun deleteOwners(owners: Owners)

    @Query("SELECT * FROM owners")
    fun getAllOwners(): Flow<List<Owners>>

    @Query("SELECT * FROM owners where buildingId = 0")
    fun getAllMenuOwners(): Flow<List<Owners>>

    @Query("SELECT * FROM owners WHERE buildingId = :buildingId")
    fun getOwnersForBuilding(buildingId: Long): Flow<List<Owners>>
}
