package com.example.delta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.entity.Role
import com.example.delta.data.model.AppDatabase
import com.example.delta.enums.PermissionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PermissionViewModel (application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val authDao = database.authorizationDao()


//    suspend fun checkUserAccess(
//        roleId: Long,
//        objectId: Long,
//        requiredAction: PermissionLevel
//    ): Boolean {
//        return authDao.hasPermission(roleId, objectId, requiredAction)
//    }
//
//    fun updatePermissions(
//        roleId: Long,
//        objectIds: List<Long>,
//        newLevel: PermissionLevel
//    ) {
//        viewModelScope.launch(Dispatchers.IO) {
//            objectIds.forEach { objectId ->
//                authDao.updatePermission(roleId, objectId, newLevel)
//            }
//        }
//    }


}
