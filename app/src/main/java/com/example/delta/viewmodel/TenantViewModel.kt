package com.example.delta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Tenants
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TenantViewModel (application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val tenantsDao = database.tenantDao()


    fun insertTenants(tenants: Tenants) = viewModelScope.launch {
        tenantsDao.insertTenants(tenants)
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

}
