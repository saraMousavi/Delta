package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.BuildingOwnerCrossRef
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.OwnersUnitsCrossRef
import com.example.delta.data.entity.UnitWithDang
import kotlinx.coroutines.flow.Flow

@Dao
interface OwnersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwners(owners: Owners) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildingOwner(buildingOwners: BuildingOwnerCrossRef) : Long

    @Delete
    suspend fun deleteOwners(owners: Owners)

    // Delete tenants linked to building
    @Query("DELETE FROM owners_units_cross_ref WHERE ownerId IN (SELECT ownerId FROM owners WHERE ownerId = :ownerId)")
    suspend fun deleteOwnersWithUnits(ownerId: Long)

    @Update
    suspend fun updateOwner(owners: Owners)

    @Query("SELECT * FROM owners")
    fun getAllOwners(): Flow<List<Owners>>

    @Query("SELECT * FROM owners")
    fun getAllMenuOwners(): Flow<List<Owners>>

    // Owner-Unit relationships
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwnerUnitCrossRef(crossRef: OwnersUnitsCrossRef)



    @Query("SELECT * FROM Owners")
    suspend fun getOwners(): List<Owners>

    // Helper function to get owner details
    @Query("SELECT * FROM Owners WHERE ownerId = :ownerId")
    suspend fun getOwnerById(ownerId: Long): Owners


    @Query("""
    SELECT units.*, owners_units_cross_ref.dang
    FROM units
    INNER JOIN owners_units_cross_ref ON units.unitId = owners_units_cross_ref.unitId
    WHERE owners_units_cross_ref.ownerId = :ownerId
""")
    fun getUnitsWithDangForOwner(ownerId: Long): List<UnitWithDang>


    @Query("""
    SELECT o.* FROM Owners o
    JOIN building_owner_cross_ref owb ON o.ownerId = owb.ownerId
    WHERE owb.buildingId = :buildingId
""")
    suspend fun getOwnersForBuilding(buildingId: Long?): List<Owners>

    // Find owners linked to units
    @Query("SELECT ownerId FROM owners_units_cross_ref WHERE unitId IN (SELECT unitId FROM units WHERE buildingId = :buildingId)")
    suspend fun getOwnerIdsByBuilding(buildingId: Long): List<Long>

    // Delete owners linked to building
    @Query("DELETE FROM owners WHERE ownerId IN (SELECT ownerId FROM owners_units_cross_ref WHERE unitId IN (SELECT unitId FROM units WHERE buildingId = :buildingId))")
    suspend fun deleteOwnersForBuilding(buildingId: Long)

    @Query("SELECT * FROM owners_units_cross_ref WHERE ownerId = :ownerId")
    fun getOwnersWithUnits(ownerId: Long): List<OwnersUnitsCrossRef>


    @Query("SELECT * FROM owners_units_cross_ref WHERE ownerId in ( :ownerId )")
    fun getOwnersWithUnitsList(ownerId: List<Long>): List<OwnersUnitsCrossRef>

    @Transaction
    @Query("SELECT * FROM owners_units_cross_ref")
    fun getOwnersUnitsCrossRef(): List<OwnersUnitsCrossRef>




}
