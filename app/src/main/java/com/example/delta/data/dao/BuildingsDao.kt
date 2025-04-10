package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.BuildingOwnerCrossRef
import com.example.delta.data.entity.BuildingTenantCrossRef
import com.example.delta.data.entity.BuildingWithOwnersAndTenants
import com.example.delta.data.entity.BuildingWithType
import com.example.delta.data.entity.BuildingWithUsage
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.Tenants
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingsDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildingOwnerCrossRef(crossRef: BuildingOwnerCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildingTenantCrossRef(crossRef: BuildingTenantCrossRef)

    @Insert
    suspend fun insertBuilding(building: Buildings): Long

    @Insert
    suspend fun insertOwner(owner: Owners): Long

    @Insert
    suspend fun insertTenant(tenant: Tenants): Long

    // Query buildings with owners and tenants
    @Transaction
    @Query("SELECT * FROM buildings WHERE buildingId = :buildingId")
    suspend fun getBuildingWithOwnersAndTenants(buildingId: Long): BuildingWithOwnersAndTenants

    @Delete
    suspend fun deleteBuildings(buildings: Buildings)

    @Query("SELECT * FROM buildings")
    fun getAllBuildings(): Flow<List<Buildings>>


    @Transaction
    @Query("SELECT * FROM buildings WHERE buildingId = :buildingId")
    fun getBuildingWithType(buildingId: Long): Flow<BuildingWithType>

    @Transaction
    @Query("SELECT * FROM buildings WHERE buildingId = :buildingId")
    fun getBuildingWithUsage(buildingId: Long): Flow<BuildingWithUsage>
}
