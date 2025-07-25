package com.example.delta.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.delta.enums.Roles

@Entity(
    tableName = "role"
)
data class Role(
    @PrimaryKey(autoGenerate = true) val roleId: Long = 0,
    @ColumnInfo(name = "roleName") val roleName: Roles,
    @ColumnInfo(name = "role_description") val roleDescription: String,
)
