package com.example.delta.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user",
    foreignKeys = [ForeignKey(
        entity = Role::class,
        parentColumns = ["roleId"],
        childColumns = ["roleId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["roleId"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) var userId: Long = 0,
    @ColumnInfo(name = "mobile_number") val mobileNumber: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "roleId") val roleId: Long, // owner, tenant, manager, guest
)
