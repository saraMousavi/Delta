package com.example.delta.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Units

@Dao
interface UnitsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: Units): Long

    @Update
    suspend fun updateUnit(unit: Units)

    @Delete
    suspend fun deleteUnit(unit: Units)

    @Query("SELECT * FROM units WHERE buildingId = :buildingId")
    suspend fun getUnitsByBuildingId(buildingId: Long?): List<Units>

    @Query("SELECT * FROM units where buildingId = :buildingId")
    suspend fun getUnits(buildingId: Long): List<Units>

    @Query("SELECT * FROM units")
    suspend fun getAllUnits(): List<Units>


    @Query("SELECT * FROM Units WHERE unitId IN (:unitIds)")
    fun getUnitsByIds(unitIds: List<Long>): List<Units>

    @Query("DELETE FROM units WHERE buildingId = :buildingId")
    suspend fun deleteUnitsForBuilding(buildingId: Long)

    @Query("""
    SELECT buildings.* FROM buildings
    INNER JOIN units ON units.buildingId = buildings.buildingId
    WHERE units.unitId = :unitId
""")
    fun getBuildingForUnit(unitId: Long): Buildings


    @Query("SELECT * FROM units where unitId = :unitId")
    fun getUnit(unitId: Long): Units

    @Query("SELECT COUNT(*) FROM units WHERE buildingId = :buildingId")
    suspend fun countUnits(buildingId: Long): Int


    @Query("""
    SELECT u.* FROM units u
    JOIN tenants_units_cross_ref tuc ON u.unitId = tuc.unitId
    WHERE u.buildingId = :buildingId
    """)
    suspend fun getActiveUnits(buildingId: Long): List<Units>

    @Query("""
    SELECT distinct u.* FROM units u
    INNER JOIN owners_units_cross_ref ou ON u.unitId = ou.unitId
    WHERE ou.ownerId IN (:ownerIds)
""")
    suspend fun getUnitsByOwnerIds(ownerIds: List<Long>): List<Units>

    @Query("""
    SELECT unitId, SUM(dang) as totalDang
    FROM owners_units_cross_ref
    GROUP BY unitId
""")
    suspend fun getDangSumsForAllUnits(): List<UnitDangSum>

    data class UnitDangSum(val unitId: Long, val totalDang: Double)

}