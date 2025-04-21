package com.example.delta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Tenants
import com.example.delta.data.entity.TenantsUnitsCrossRef
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TenantViewModel (application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val tenantsDao = database.tenantDao()


    suspend fun insertTenants(tenant: Tenants): Long {
        return withContext(Dispatchers.IO) {
            tenantsDao.insertTenants(tenant)
        }
    }

    fun deleteTenants(tenants: Tenants) = viewModelScope.launch {
        tenantsDao.deleteTenants(tenants)
    }

    fun getAllTenants(): Flow<List<Tenants>> {
        return tenantsDao.getAllTenants()
    }
    fun getAllMenuTenants(): Flow<List<Tenants>> {
        return tenantsDao.getAllMenuTenants()
    }

    suspend fun getAllTenantUnitRelations(): List<TenantsUnitsCrossRef> {
        return tenantsDao.getAllTenantUnitRelationships()
    }

}
