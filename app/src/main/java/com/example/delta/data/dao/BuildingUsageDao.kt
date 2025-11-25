// BuildingUsageDao.kt
package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.BuildingUsages
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingUsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildingUsage(buildingUsages: BuildingUsages)

    @Delete()
    suspend fun deleteBuildingUsage(buildingUsages: BuildingUsages)

    @Query("SELECT * FROM building_usages")
    fun getAllBuildingUsages(): Flow<List<BuildingUsages>>

    @Query("SELECT * FROM building_usages where buildingUsageId =:buildingUsageId")
    suspend fun getBuildingUsages(buildingUsageId: Long): BuildingUsages?

    @Query("SELECT building_usage_name FROM building_usages WHERE buildingUsageId = :buildingUsageId")
    suspend fun getBuildingUsageName(buildingUsageId: Long?): String?

    @Query("SELECT buildingUsageId FROM building_usages WHERE building_usage_name = :buildingUsageName")
    suspend fun getBuildingUsageByName(buildingUsageName: String?): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertALL(list: List<BuildingUsages>)

    @Query("DELETE FROM building_usages")
    suspend fun deleteALL()
}
