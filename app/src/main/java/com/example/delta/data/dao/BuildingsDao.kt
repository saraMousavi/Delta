package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.BuildingWithCosts
import com.example.delta.data.entity.BuildingWithEarnings
import com.example.delta.data.entity.BuildingWithType
import com.example.delta.data.entity.BuildingWithUsage
import com.example.delta.data.entity.Buildings
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildings(buildings: Buildings)

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
