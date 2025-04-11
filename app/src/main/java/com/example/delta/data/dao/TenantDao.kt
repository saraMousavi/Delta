package com.example.delta.data.dao

import androidx.room.*
import com.example.delta.data.entity.Tenants
import kotlinx.coroutines.flow.Flow

@Dao
interface TenantDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTenants(tenants: Tenants)

    @Delete()
    suspend fun deleteTenants(tenants: Tenants)

    @Query("SELECT * FROM tenants")
    fun getAllTenants(): Flow<List<Tenants>>

    @Query("SELECT * FROM tenants")
    fun getAllMenuTenants(): Flow<List<Tenants>>

}
