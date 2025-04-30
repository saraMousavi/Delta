package com.example.delta.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.delta.data.entity.AuthorizationField
import com.example.delta.data.entity.AuthorizationObject
import com.example.delta.data.entity.RoleAuthorizationObjectCrossRef
import com.example.delta.enums.PermissionLevel

@Dao
interface AuthorizationDao {
    // Authorization Objects
    @Insert
    suspend fun insertAuthorizationObject(obj: AuthorizationObject): Long

    @Query("SELECT * FROM authorization_objects")
    fun getAllAuthorizationObjects(): List<AuthorizationObject>

    @Query("SELECT COUNT(*) FROM authorization_fields")
    suspend fun getFieldCount(): Int

    // Authorization Fields
    @Insert
    suspend fun insertAuthorizationField(field: AuthorizationField): Long

    @Query("SELECT * FROM authorization_fields WHERE objectId = :objectId")
    fun getFieldsForObject(objectId: Long): List<AuthorizationField>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoleAuthorizationObjectCrossRef(crossRef: RoleAuthorizationObjectCrossRef)

    // Role-Object Relationships
    @Insert
    suspend fun insertRoleObjectCrossRef(crossRef: RoleAuthorizationObjectCrossRef)

    @Transaction
    @Query("SELECT * FROM authorization_objects WHERE objectId IN " +
            "(SELECT objectId FROM role_authorization_object WHERE roleId = :roleId)")
    suspend fun getAuthorizedObjectsForRole(roleId: Long): List<AuthorizationObject>

    // Field Permissions (indirect through objects)
    @Transaction
    @Query("SELECT * FROM authorization_fields WHERE objectId IN " +
            "(SELECT objectId FROM role_authorization_object WHERE roleId = :roleId)")
    suspend fun getAuthorizedFieldsForRole(roleId: Long): List<AuthorizationField>

    @Query("SELECT * FROM role_authorization_object WHERE roleId = :roleId")
    suspend fun getPermissionsForRole(roleId: Long): List<RoleAuthorizationObjectCrossRef>

    @Transaction
    suspend fun updatePermission(
        roleId: Long,
        objectId: Long,
        level: PermissionLevel
    ) {
        insertRoleObjectCrossRef(
            RoleAuthorizationObjectCrossRef(
                roleId = roleId,
                objectId = objectId,
                permissionLevel = level.value
            )
        )
    }

    // Object-level permission
    @Query("""
        SELECT permissionLevel FROM role_authorization_object
        WHERE roleId = :roleId AND objectId = :objectId
        LIMIT 1
    """)
    suspend fun getObjectPermission(roleId: Long, objectId: Long): Int?

    // Field-level permission (optional, for overrides)
    @Query("""
        SELECT permissionLevel FROM role_authorization_field
        WHERE roleId = :roleId AND fieldId = :fieldId
        LIMIT 1
    """)
    suspend fun getFieldPermission(roleId: Long, fieldId: Long): Int?

    // Get field entity by name and objectId
    @Query("""
        SELECT * FROM authorization_fields
        WHERE objectId = :objectId AND name = :fieldName
        LIMIT 1
    """)
    suspend fun getFieldByName(objectId: Long, fieldName: Int): AuthorizationField?

    @Transaction
    suspend fun hasPermission(
        roleId: Long,
        objectId: Long,
        requiredLevel: PermissionLevel
    ): Boolean {
        val permission = getPermission(roleId, objectId)
        return when {
            permission == null -> false
            permission.permissionLevelEnum == PermissionLevel.FULL -> true
            else -> permission.permissionLevelEnum.value >= requiredLevel.value
        }
    }

    // Add to AuthorizationDao
    @Query("SELECT COUNT(*) FROM authorization_objects")
    suspend fun getCount(): Int

    @Query("""
        SELECT * FROM role_authorization_object 
        WHERE roleId = :roleId AND objectId = :objectId
        LIMIT 1
    """)
    suspend fun getPermission(roleId: Long, objectId: Long): RoleAuthorizationObjectCrossRef?
}