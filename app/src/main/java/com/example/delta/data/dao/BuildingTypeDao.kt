// BuildingTypeDao.kt
package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.BuildingTypes
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingTypeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildingType(buildingTypes: BuildingTypes)

    @Delete()
    suspend fun deleteBuildingType(buildingTypes: BuildingTypes)

    @Query("SELECT * FROM building_types")
    fun getAllBuildingTypes(): Flow<List<BuildingTypes>>


    @Query("SELECT * FROM building_types where buildingTypeId =:buildingTypeId")
    suspend fun getBuildingTypes(buildingTypeId : Long): BuildingTypes?

    @Query("SELECT building_type_name FROM building_types WHERE buildingTypeId = :buildingTypeId")
    suspend fun getBuildingTypeName(buildingTypeId: Long?): String?

    @Query("SELECT buildingTypeId FROM building_types WHERE building_type_name = :buildingTypeName")
    suspend fun getBuildingTypeIdByName(buildingTypeName: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertALL(list: List<BuildingTypes>)

    @Query("DELETE FROM building_types")
    suspend fun deleteALL()
}
