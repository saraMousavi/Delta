package com.example.delta.init

import android.content.Context
import com.example.delta.data.model.AppDatabase
import com.example.delta.enums.AuthObject
import com.example.delta.enums.PermissionLevel

object AuthPermissionChecker {
    suspend fun checkAccess(
        context: Context,
        authObject: AuthObject,
        requiredLevel: PermissionLevel
    ): Boolean {
        val db = AppDatabase.getDatabase(context)
        val currentRoleId = getCurrentRoleId(context) // Implement user session management

        return db.authorizationDao().run {
            hasPermission(
                roleId = currentRoleId,
                objectId = authObject.id,
                requiredLevel = requiredLevel
            )
        }
    }

    private suspend fun getCurrentRoleId(context: Context): Long {
        // Retrieve from SharedPreferences or database
        return 1L // Default admin role
    }
}