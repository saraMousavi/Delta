package com.example.delta.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class UserWithRole(
    @Embedded
    val user: User,

    @Relation(
        parentColumn = "roleId",
        entityColumn = "roleId"
    )
    val role: Role
)