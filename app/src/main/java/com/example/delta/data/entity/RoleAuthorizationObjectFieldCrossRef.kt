package com.example.delta.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.delta.enums.PermissionLevel

@Entity(
    primaryKeys = ["roleId", "fieldId"],
    tableName = "role_authorization_object_field_cross_ref",
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
        ),
        ForeignKey(
            entity = AuthorizationObject::class,
            parentColumns = ["objectId"],
            childColumns = ["objectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Buildings::class,
            parentColumns = ["buildingId"],
            childColumns = ["buildingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("roleId"),
        Index("fieldId"),
        Index("objectId"),
        Index("buildingId"),
        Index("userId")
    ]
)
data class RoleAuthorizationObjectFieldCrossRef(
    val roleId: Long,
    val userId: Long,
    val buildingId: Long,
    val objectId: Long,
    val fieldId: Long,
    val permissionLevel: PermissionLevel // 0=read, 1=write, 2= delete 3=full
)
