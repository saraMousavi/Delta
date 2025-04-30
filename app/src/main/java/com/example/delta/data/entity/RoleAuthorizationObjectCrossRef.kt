package com.example.delta.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.delta.enums.PermissionLevel

@Entity(
    primaryKeys = ["roleId", "objectId"],
    tableName = "role_authorization_object",
    foreignKeys = [
        ForeignKey(
            entity = Role::class,
            parentColumns = ["roleId"],
            childColumns = ["roleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AuthorizationObject::class,
            parentColumns = ["objectId"],
            childColumns = ["objectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("roleId"),
        Index("objectId")
    ]
)
data class RoleAuthorizationObjectCrossRef(
    val roleId: Long,
    val objectId: Long,
    val permissionLevel: Int // 0=read, 1=write, 2= delete 3=full
){
    // Helper property (not stored in DB)
    val permissionLevelEnum: PermissionLevel
        get() = PermissionLevel.fromValue(permissionLevel)
}

