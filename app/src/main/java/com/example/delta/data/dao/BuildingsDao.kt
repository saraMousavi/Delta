package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUploadedFileCrossRef
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.entity.BuildingWithType
import com.example.delta.data.entity.BuildingWithTypesAndUsages
import com.example.delta.data.entity.BuildingWithUsage
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.UsersBuildingsCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingsDao {


    @Insert
    suspend fun insertBuilding(building: Buildings): Long

    @Update
    suspend fun updateBuilding(buildings: Buildings)

    @Delete
    suspend fun deleteBuildings(buildings: Buildings)

    @Query("SELECT * FROM buildings")
    fun getAllBuildingsList(): List<Buildings>

    @Query("""
    SELECT b.*, r.roleName,    
            bt.building_type_name AS buildingTypeName, 
            bu.building_usage_name AS buildingUsageName
        FROM buildings b
            LEFT JOIN building_types bt ON b.buildingTypeId = bt.buildingTypeId
                LEFT JOIN building_usages bu ON b.buildingUsageId = bu.buildingUsageId
            INNER JOIN users_buildings_cross_ref ub ON b.buildingId = ub.buildingId
            INNER JOIN user u ON ub.userId = u.userId
            INNER JOIN role r ON u.roleId = r.roleId
            WHERE u.userId = :userId
""")
    fun getBuildingsWithUserRole(userId: Long): List<BuildingWithTypesAndUsages>


    @Query("""
    SELECT b.*
        FROM buildings b
            INNER JOIN users_buildings_cross_ref ub ON b.buildingId = ub.buildingId
            WHERE b.userId = :userId
""")
    fun getBuildingsForUser(userId: Long): List<Buildings>

    @Query("""
    SELECT * FROM  users_buildings_cross_ref ub 
""")
    fun getBuildingsWithUserRoles(): List<UsersBuildingsCrossRef>

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

    @Query("SELECT * FROM buildings")
    suspend fun getBuildings(): List<Buildings>

    @Query("SELECT * FROM buildings WHERE buildingId = :buildingId")
    fun getBuilding(buildingId: Long): Buildings

    @Query("SELECT * FROM buildings WHERE name = :buildingName")
    fun getBuilding(buildingName: String): Buildings


    @Query("SELECT bt.building_type_name FROM building_types bt WHERE bt.buildingTypeId = :buildingTypeId")
    suspend fun getBuildingTypeName(buildingTypeId: Long?): String

    @Query("SELECT bu.building_usage_name FROM building_usages bu WHERE bu.buildingUsageId = :buildingUsageId")
    suspend fun getBuildingUsageName(buildingUsageId: Long?): String

    @Query("SELECT * FROM Costs WHERE buildingId = :buildingId")
    suspend fun getCostsForBuilding(buildingId: Int): List<Costs>

    @Update
    suspend fun updateCost(cost: Costs)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: BuildingUploadedFileCrossRef)


    @Update
    suspend fun updateCrossRef(crossRef: BuildingUploadedFileCrossRef)


    // Delete cross-references by fileId(s) (optional)
    @Query("DELETE FROM building_uploaded_files_cross_ref WHERE fileId IN (:fileIds)")
    fun deleteCrossRefsByFileIds(fileIds: List<Long>)

    @Query("""
    SELECT SUM(CAST(number_of_tenants AS INTEGER)) 
    FROM tenants t
    INNER JOIN building_tenant_cross_ref b ON t.tenantId = b.tenantId
    WHERE b.buildingId = :buildingId
""")
    fun getTotalResidentsInBuilding(buildingId: Long): Int

}
