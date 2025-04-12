package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.OwnersUnitsCrossRef
import com.example.delta.data.entity.Units
import kotlinx.coroutines.flow.Flow

@Dao
interface OwnersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwners(owners: Owners) : Long

    @Delete()
    suspend fun deleteOwners(owners: Owners)

    @Query("SELECT * FROM owners")
    fun getAllOwners(): Flow<List<Owners>>

    @Query("SELECT * FROM owners")
    fun getAllMenuOwners(): Flow<List<Owners>>

    // Owner-Unit relationships
    @Insert
    suspend fun insertOwnerUnitCrossRef(crossRef: OwnersUnitsCrossRef)


    @Query("SELECT * FROM Owners")
    suspend fun getOwners(): List<Owners>

    @Query("SELECT * FROM Units WHERE unitId IN (SELECT unitId FROM OwnersUnitsCrossRef WHERE ownerId = :ownerId)")
    suspend fun getUnitsForOwner(ownerId: Int): List<Units>

    // Helper function to get owner details
    @Query("SELECT * FROM Owners WHERE ownerId = :ownerId")
    suspend fun getOwnerById(ownerId: Int): Owners

    @Query("""
    SELECT o.* FROM Owners o
    JOIN OwnersUnitsCrossRef ouc ON o.ownerId = ouc.ownerId
    JOIN Units u ON ouc.unitId = u.unitId
    WHERE u.buildingId = :buildingId
""")
    suspend fun getOwnersForBuilding(buildingId: Long): List<Owners>


}
