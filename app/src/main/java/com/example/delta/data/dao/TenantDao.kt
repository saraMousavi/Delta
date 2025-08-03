package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.TenantWithRelation
import com.example.delta.data.entity.Tenants
import com.example.delta.data.entity.TenantsUnitsCrossRef
import com.example.delta.data.entity.TenantsWithUnits
import com.example.delta.data.entity.Units
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

    @Query("SELECT * FROM Tenants where tenantId = :tenantId")
    fun getTenant(tenantId: Long): Tenants


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
    fun getTenantsForBuilding(buildingId: Long): Flow<List<Tenants>>


    // Find tenants linked to units
    @Query("SELECT tenantId FROM tenants_units_cross_ref WHERE unitId IN (SELECT unitId FROM units WHERE buildingId = :buildingId)")
    suspend fun getTenantIdsByBuilding(buildingId: Long): List<Long>

    @Update
    suspend fun updateTenant(tenants: Tenants)

    // Delete tenants linked to building
    @Query("DELETE FROM tenants WHERE tenantId IN (SELECT tenantId FROM tenants_units_cross_ref WHERE unitId IN (SELECT unitId FROM units WHERE buildingId = :buildingId))")
    suspend fun deleteTenantsForBuilding(buildingId: Long)

    @Transaction
    @Query("SELECT * FROM Tenants WHERE tenantId IN " +
            "(SELECT tenantId FROM tenants_units_cross_ref WHERE unitId = :unitId)")
    suspend fun getTenantsForUnit(unitId: Long): List<Tenants>


    @Transaction
    @Query("SELECT * FROM Tenants WHERE tenantId IN " +
            "(SELECT tenantId FROM tenants_units_cross_ref WHERE unitId = :unitId" +
            " and status = 'فعال')")
    fun getActiveTenantForUnit(unitId: Long): Tenants?


    @Transaction
    @Query("SELECT number_of_tenants FROM Tenants WHERE tenantId IN " +
            "(SELECT tenantId FROM tenants_units_cross_ref WHERE unitId = :unitId" +
            " and status = 'فعال')")
    suspend fun getNumberOfUnitsTenantForUnit(unitId: Long): String?

    // Get cross-reference entries for a unit
    @Query("SELECT * FROM tenants_units_cross_ref WHERE unitId = :unitId")
    suspend fun getTenantUnitRelationships(unitId: Long): List<TenantsUnitsCrossRef>

    @Query("SELECT * FROM tenants_units_cross_ref WHERE tenantId = :tenantId")
    suspend fun getTenantUnitCrossRef(tenantId: Long): TenantsUnitsCrossRef?


    @Update
    suspend fun updateTenantUnitCrossRef(crossRef: TenantsUnitsCrossRef)

    @Query("DELETE FROM tenants_units_cross_ref WHERE tenantId = :tenantId")
    suspend fun deleteTenantUnitCrossRef(tenantId: Long)

    @Query("SELECT * FROM tenants_units_cross_ref")
    suspend fun getAllTenantUnitRelationships(): List<TenantsUnitsCrossRef>

    @Query("""
    SELECT tenants.*, crossRef.startDate, crossRef.endDate, crossRef.status
    FROM tenants
    INNER JOIN tenants_units_cross_ref AS crossRef ON tenants.tenantId = crossRef.tenantId
    WHERE crossRef.unitId = :unitId
""")
    suspend fun getTenantsWithRelationForUnit(unitId: Long): List<TenantWithRelation>

    @Query("""
    SELECT tenants.*, crossRef.startDate, crossRef.endDate, crossRef.status
    FROM tenants
    INNER JOIN tenants_units_cross_ref AS crossRef ON tenants.tenantId = crossRef.tenantId
    WHERE crossRef.unitId = :unitId and crossRef.status = "فعال"  LIMIT 1
""")
    suspend fun getActiveTenantsWithRelationForUnit(unitId: Long): TenantWithRelation?

    @Transaction
    @Query("""
    SELECT DISTINCT t.* FROM Tenants t
    INNER JOIN tenants_units_cross_ref tur ON t.tenantId = tur.tenantId
    INNER JOIN Units u ON tur.unitId = u.unitId
    WHERE u.buildingId = :buildingId
      AND t.status = 'فعال'
      -- Optionally add more filters on tur.startDate, tur.endDate, or t.status if needed
""")
    suspend fun getActiveTenantsForBuilding(buildingId: Long): List<Tenants>



    @Query("""
    SELECT * FROM tenants_units_cross_ref 
    WHERE unitId = :unitId 
    AND status = :status
""")
    fun getActiveTenantUnitRelationships(unitId: Long, status: String): TenantsUnitsCrossRef?

    @Query("SELECT * FROM Units WHERE unitId IN (SELECT unitId FROM tenants_units_cross_ref WHERE tenantId = :tenantId) Limit 1")
    suspend fun getUnitForTenant(tenantId: Long): Units?

    @Query("SELECT * FROM Units")
    suspend fun getAllUnits(): List<Units>

    @Query("SELECT * FROM Units where unitId = :unitId")
    suspend fun getUnit(unitId: Long): Units

    @Query("""
    SELECT *
    FROM tenants
    WHERE mobile_number = :mobileNumber
""")
    suspend fun getTenantForUserMobileNumber(mobileNumber: String): Tenants?

    @Query("""
        SELECT * FROM tenants_units_cross_ref
        WHERE tenantId = :tenantId AND unitId = :unitId LIMIT 1
    """)
    suspend fun getCrossRefForTenantUnit(tenantId: Long, unitId: Long): TenantsUnitsCrossRef?

}

//      , active: String  AND status = :active