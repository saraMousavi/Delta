package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.Tenants
import com.example.delta.data.entity.TenantsUnitsCrossRef
import com.example.delta.data.entity.TenantsWithUnits
import kotlinx.coroutines.flow.Flow

@Dao
interface TenantDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTenants(tenants: Tenants) : Long

    @Delete()
    suspend fun deleteTenants(tenants: Tenants)

    @Query("SELECT * FROM tenants")
    fun getAllTenants(): Flow<List<Tenants>>

    @Query("SELECT * FROM Tenants")
    suspend fun getTenants(): List<Tenants>


    @Query("SELECT * FROM tenants")
    fun getAllMenuTenants(): Flow<List<Tenants>>

    @Insert
    suspend fun insertTenantUnitCrossRef(crossRef: TenantsUnitsCrossRef)

    @Transaction
    @Query("SELECT * FROM tenants WHERE tenantId = :tenantId")
    suspend fun getTenantWithUnits(tenantId: Long): TenantsWithUnits

    @Query("""
    SELECT t.* FROM tenants t
    JOIN tenants_units_cross_ref tuc ON t.tenantId = tuc.tenantId
    JOIN Units u ON tuc.unitId = u.unitId
    WHERE u.buildingId = :buildingId
    """)
    suspend fun getTenantsForBuilding(buildingId: Long): List<Tenants>

    // Find tenants linked to units
    @Query("SELECT tenantId FROM tenants_units_cross_ref WHERE unitId IN (SELECT unitId FROM units WHERE buildingId = :buildingId)")
    suspend fun getTenantIdsByBuilding(buildingId: Long): List<Long>

    // Delete tenants linked to building
    @Query("DELETE FROM tenants WHERE tenantId IN (SELECT tenantId FROM tenants_units_cross_ref WHERE unitId IN (SELECT unitId FROM units WHERE buildingId = :buildingId))")
    suspend fun deleteTenantsForBuilding(buildingId: Long)

}
