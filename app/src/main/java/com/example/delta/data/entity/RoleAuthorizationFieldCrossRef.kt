package com.example.delta.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.delta.enums.PermissionLevel

@Entity(
    primaryKeys = ["roleId", "fieldId"],
    tableName = "role_authorization_field",
    foreignKeys = [
        ForeignKey(
            entity = Role::class,
            parentColumns = ["roleId"],
            childColumns = ["roleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AuthorizationField::class,
            parentColumns = ["fieldId"],
            childColumns = ["fieldId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("roleId"),
        Index("fieldId")
    ]
)
data class RoleAuthorizationFieldCrossRef(
    val roleId: Long,
    val fieldId: Long,
    val permissionLevel: Int // 0=read, 1=write, 2= delete 3=full
){
    // Helper property (not stored in DB)
    val permissionLevelEnum: PermissionLevel
        get() = PermissionLevel.fromValue(permissionLevel)
}

