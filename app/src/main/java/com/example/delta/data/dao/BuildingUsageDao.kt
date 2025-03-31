// BuildingUsageDao.kt
package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.BuildingUsage
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingUsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildingUsage(buildingUsage: BuildingUsage)

    @Delete()
    suspend fun deleteBuildingUsage(buildingUsage: BuildingUsage)

    @Query("SELECT * FROM building_usage")
    fun getAllBuildingUsages(): Flow<List<BuildingUsage>>
}
