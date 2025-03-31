package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.BuildingWithCosts
import com.example.delta.data.entity.BuildingWithIncomes
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

    @Transaction // Indicates that this query involves multiple tables.
    @Query("SELECT * FROM buildings WHERE buildingId = :buildingId")
    fun getBuildingWithIncomes(buildingId: Long): Flow<BuildingWithIncomes>

    @Transaction // Indicates that this query involves multiple tables.
    @Query("SELECT * FROM buildings WHERE buildingId = :buildingId")
    fun getBuildingWithCosts(buildingId: Long): Flow<BuildingWithCosts>
}
