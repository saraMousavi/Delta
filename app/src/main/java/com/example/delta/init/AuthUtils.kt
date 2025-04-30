package com.example.delta.init

import android.content.Context
import com.example.delta.data.dao.AuthorizationDao
import com.example.delta.data.model.AppDatabase
import com.example.delta.enums.AuthObject
import com.example.delta.enums.PermissionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AuthUtils {
    // Example: get current user roleId from SharedPreferences or Session
    fun getCurrentRoleId(context: Context): Long {
        // For demo, return a fixed roleId.
        // Replace this with your own logic (e.g., from SharedPreferences or a SessionManager).
        val sharedPref = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        return sharedPref.getLong("role_id", 1L) // 1L = admin by default
    }

    // Check if the current role has permission for the field
    suspend fun hasFieldPermission(
        authDao: AuthorizationDao,
        roleId: Long,
        objectId: Long,
        fieldName: Int,
        required: PermissionLevel = PermissionLevel.READ
    ): Boolean = withContext(Dispatchers.IO) {
        val field = authDao.getFieldByName(objectId, fieldName)
        val fieldPerm = field?.let { authDao.getFieldPermission(roleId, it.fieldId) }
        val objectPerm = authDao.getObjectPermission(roleId, objectId)
        val effectivePerm = fieldPerm ?: objectPerm
        effectivePerm != null && effectivePerm >= required.value
    }

    // Optional: check permission for an activity/object
    suspend fun checkActivityPermission(
        context: Context,
        activityClass: Class<*>,
        requiredLevel: PermissionLevel
    ): Boolean = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(context)
        val authObject = AuthObject.entries.find { it.name == activityClass.simpleName.uppercase() }
        val roleId = getCurrentRoleId(context)
        if (authObject != null) {
            val perm = db.authorizationDao().getObjectPermission(roleId, authObject.id)
            perm != null && perm >= requiredLevel.value
        } else false
    }
}