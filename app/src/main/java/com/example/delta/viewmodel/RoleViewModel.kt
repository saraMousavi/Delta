package com.example.delta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.entity.Role
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.launch

class RoleViewModel (application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val rolesDao = database.roleDao()


    fun insertRole(roles: Role) = viewModelScope.launch {
        rolesDao.insertRole(roles)
    }

    fun deleteRole(roles: Role) = viewModelScope.launch {
        rolesDao.deleteRole(roles)
    }


    suspend fun getRoles(): List<Role> {
        return rolesDao.getRoles()
    }

}
