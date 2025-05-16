package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.OwnerWithBuildings
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.OwnersUnitsCrossRef
import com.example.delta.data.entity.UnitWithDang
import com.example.delta.data.entity.Units
import kotlinx.coroutines.flow.Flow

@Dao
interface OwnersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwners(owners: Owners) : Long

    @Delete()
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
    @Insert
    suspend fun insertOwnerUnitCrossRef(crossRef: OwnersUnitsCrossRef)

    @Insert
    suspend fun insertOwnerWithBuild(ownersWithBuildings: OwnerWithBuildings)


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
    suspend fun getUnitsWithDangForOwner(ownerId: Long): List<UnitWithDang>


    @Query("""
    SELECT o.* FROM Owners o
    JOIN owners_with_building owb ON o.ownerId = owb.ownerId
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
    suspend fun getOwnersWithUnits(ownerId: Long): List<OwnersUnitsCrossRef>

    @Query("SELECT * FROM owners_units_cross_ref")
    suspend fun getOwnersUnitsCrossRef(): List<OwnersUnitsCrossRef>


}
