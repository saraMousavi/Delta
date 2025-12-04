package com.example.delta.init

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.delta.enums.PermissionLevel
import com.example.delta.viewmodel.SharedViewModel

object AuthUtils {
    fun getCurrentRoleId(context: Context): Long {
        val sharedPref = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        return sharedPref.getLong("role_id", 1L) // 1L = admin by default
    }

    @Composable
    fun checkFieldPermission(
        userId: Long,
        targetFieldNameRes: String,
        sharedViewModel: SharedViewModel
    ): PermissionLevel? {
        val context = LocalContext.current
        val fields by sharedViewModel
            .getAuthorizationDetailsForUser(context, userId)
            .collectAsStateWithLifecycle(initialValue = emptyList())

        val fieldPermission = remember(fields, targetFieldNameRes) {
            fields.find { it.field.name == targetFieldNameRes }
        }

        return fieldPermission?.crossRef?.permissionLevel
    }

}