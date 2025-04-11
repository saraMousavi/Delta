package com.example.delta.data.dao

import androidx.room.*
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

}
