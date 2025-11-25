package com.example.delta.data.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.delta.data.entity.AuthorizationField
import com.example.delta.data.entity.AuthorizationObject
import com.example.delta.data.entity.RoleAuthorizationObjectFieldCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthorizationDao {
    // Authorization Objects
    @Insert
    suspend fun insertAuthorizationObject(obj: AuthorizationObject): Long

    @Query("SELECT * FROM authorization_objects")
    fun getAllAuthorizationObjects(): List<AuthorizationObject>

    @Query("SELECT * FROM authorization_fields")
    fun getAllAuthorizationFields(): List<AuthorizationField>


    @Query("SELECT * FROM role_authorization_object_field_cross_ref")
    fun getAllRoleAuthorizationObjectFieldCrossRef(): List<RoleAuthorizationObjectFieldCrossRef>

    @Query("SELECT COUNT(*) FROM authorization_fields")
    suspend fun getFieldCount(): Int

    // Authorization Fields
    @Insert
    suspend fun insertAuthorizationField(field: AuthorizationField): Long

    @Query("SELECT * FROM authorization_fields WHERE objectId = :objectId")
    fun getFieldsForObject(objectId: Long): List<AuthorizationField>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoleAuthorizationFieldCrossRef(crossRef: RoleAuthorizationObjectFieldCrossRef)


    // Get field entity by name and objectId
    @Query(
        """
        SELECT * FROM authorization_fields
        WHERE objectId = :objectId AND name = :fieldName
        LIMIT 1
    """
    )
    suspend fun getFieldByName(objectId: Long, fieldName: Int): AuthorizationField?

    @Query("""
        SELECT permissionLevel FROM role_authorization_object_field_cross_ref
        WHERE roleId = :roleId AND objectId = :objectId AND fieldId = :fieldId
        LIMIT 1
    """)
    suspend fun getPermissionLevel(
        roleId: Long,
        objectId: Long,
        fieldId: Long
    ): Int?

    @Query("SELECT fieldId FROM authorization_fields WHERE objectId = :objectId AND name = :fieldNameRes LIMIT 1")
    suspend fun getFieldIdByName(objectId: Long, fieldNameRes: Int): Long?



    // Add to AuthorizationDao
    @Query("SELECT COUNT(*) FROM authorization_objects")
    suspend fun getCount(): Int

    @Transaction
    @Query(
        """
SELECT 
    af.*,
    raf.permissionLevel AS cross_ref_permissionLevel,
    raf.roleId AS cross_ref_roleId,
    raf.objectId AS cross_ref_objectId,
    raf.fieldId AS cross_ref_fieldId,
    ao.name AS objectName 
FROM user_role_cross_ref urc
JOIN role_authorization_object_field_cross_ref raf 
    ON urc.roleId = raf.roleId
JOIN authorization_fields af 
    ON raf.fieldId = af.fieldId
JOIN authorization_objects ao 
    ON af.objectId = ao.objectId
WHERE urc.userId = :userId
"""
    )
    fun getFieldsWithPermissionsForUser(userId: Long): Flow<List<FieldWithPermission>>

    @Delete
    suspend fun deleteRoleAuthorizationFieldCrossRef(crossRef: RoleAuthorizationObjectFieldCrossRef)

    @Query("DELETE FROM role_authorization_object_field_cross_ref WHERE roleId = :roleId AND objectId = :objectId")
    suspend fun deleteRoleAuthorizationObjectCrossRefs(roleId: Long, objectId: Long)

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM role_authorization_object_field_cross_ref AS raf
            WHERE raf.roleId = :roleId
              AND raf.objectId = :objectId
              AND raf.fieldId IN (:fieldIds)
              AND raf.permissionLevel >= :minPermissionLevel
            LIMIT 1
        )
    """)
    fun hasAuthorizationForFields(
        roleId: Long,
        objectId: Long,
        fieldIds: List<Long>,
        minPermissionLevel: Int = 0 // e.g., 0 = read permission minimum
    ): Flow<Boolean>


    data class FieldWithPermission(
        @Embedded val field: AuthorizationField,

        @Embedded(prefix = "cross_ref_") // Add prefix to all crossRef columns
        val crossRef: RoleAuthorizationObjectFieldCrossRef,

        @ColumnInfo(name = "objectName")
        val objectName: Int
    )

    @Query("SELECT COUNT(*) FROM role_authorization_object_field_cross_ref")
    suspend fun getAuthorizationCrossRefCount(): Int



}

