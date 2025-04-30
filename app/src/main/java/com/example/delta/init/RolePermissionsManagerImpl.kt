package com.example.delta.init

import com.example.delta.data.entity.RoleAuthorization
import com.example.delta.interfaces.RolePermissionsManager

class RolePermissionsManagerImpl(
    private val authorizationData: MutableList<RoleAuthorization> = mutableListOf(),
    private var currentUserRole: String = "guest" // Default role
) : RolePermissionsManager {

    override fun getPermissionsForRole(role: String): RoleAuthorization? {
        return authorizationData.find { it.role == role }
    }

    override fun getAllPermissions(): List<RoleAuthorization> {
        return authorizationData.toList()
    }

    override fun createRolePermission(permissions: RoleAuthorization) {
        authorizationData.add(permissions)
    }

    override fun updateRolePermission(permissions: RoleAuthorization) {
        authorizationData.replaceAll {
            if (it.role == permissions.role) permissions else it
        }
    }

    override fun deleteRolePermission(roleName: String) {
        authorizationData.removeAll { it.role == roleName }
    }

    override fun getUserRole(): String {
        return currentUserRole
    }

    fun setUserRole(role: String) {
        currentUserRole = role
    }
}
