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

    @Query("SELECT building_usage_name FROM building_usages WHERE buildingUsageId = :buildingUsageId")
    suspend fun getBuildingUsageName(buildingUsageId: Long?): String?
}
