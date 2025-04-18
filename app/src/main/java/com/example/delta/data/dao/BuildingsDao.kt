package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.BuildingOwnerCrossRef
import com.example.delta.data.entity.BuildingTenantCrossRef
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.entity.BuildingWithOwnersAndTenants
import com.example.delta.data.entity.BuildingWithType
import com.example.delta.data.entity.BuildingWithTypesAndUsages
import com.example.delta.data.entity.BuildingWithUsage
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Costs
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

    @Query("SELECT * FROM buildings")
    fun getAllBuildingsList(): List<Buildings>

    @Query("DELETE FROM buildings WHERE buildingId = :buildingId")
    suspend fun deleteBuildingById(buildingId: Long)


    @Transaction
    @Query("SELECT * FROM buildings WHERE buildingId = :buildingId")
    fun getBuildingWithType(buildingId: Long): Flow<BuildingWithType>

    @Transaction
    @Query("SELECT * FROM buildings WHERE buildingId = :buildingId")
    fun getBuildingWithUsage(buildingId: Long): Flow<BuildingWithUsage>

    @Insert
    suspend fun insertBuildingType(buildingType: BuildingTypes): Long

    @Insert
    suspend fun insertBuildingUsage(buildingUsage: BuildingUsages): Long

    @Query("SELECT * FROM building_types")
    suspend fun getAllBuildingTypes(): List<BuildingTypes>

    @Query("SELECT * FROM building_usages")
    suspend fun getAllBuildingUsages(): List<BuildingUsages>

    @Query("SELECT * FROM Buildings")
    suspend fun getBuildings(): List<Buildings>

    @Query("""
        SELECT 
            b.*, 
            bt.building_type_name AS buildingTypeName, 
            bu.building_usage_name AS buildingUsageName
        FROM buildings b
        LEFT JOIN building_types bt ON b.buildingTypeId = bt.buildingTypeId
        LEFT JOIN building_usages bu ON b.buildingUsageId = bu.buildingUsageId
    """)
    fun getAllBuildingsWithTypesAndUsages(): Flow<List<BuildingWithTypesAndUsages>>

    @Query("SELECT bt.building_type_name FROM building_types bt WHERE bt.buildingTypeId = :buildingTypeId")
    suspend fun getBuildingTypeName(buildingTypeId: Long?): String

    @Query("SELECT bu.building_usage_name FROM building_usages bu WHERE bu.buildingUsageId = :buildingUsageId")
    suspend fun getBuildingUsageName(buildingUsageId: Long?): String

    @Query("SELECT * FROM Costs WHERE buildingId = :buildingId")
    suspend fun getCostsForBuilding(buildingId: Int): List<Costs>

    @Update
    suspend fun updateCost(cost: Costs)
}
