// BuildingTypeDao.kt
package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.BuildingType
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingTypeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildingType(buildingType: BuildingType)

    @Delete()
    suspend fun deleteBuildingType(buildingType: BuildingType)

    @Query("SELECT * FROM building_type")
    fun getAllBuildingTypes(): Flow<List<BuildingType>>
}
