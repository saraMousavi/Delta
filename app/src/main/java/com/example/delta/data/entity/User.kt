package com.example.delta.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.delta.enums.Gender

@Entity(
    tableName = "user",
    foreignKeys = [ForeignKey(
        entity = Role::class,
        parentColumns = ["roleId"],
        childColumns = ["roleId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["roleId"])]
)
data class User(
    @PrimaryKey(autoGenerate = true) var userId: Long = 0,
    @ColumnInfo(name = "mobile_number") val mobileNumber: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "first_name") val firstName: String = "",
    @ColumnInfo(name = "last_name") val lastName: String = "",
    @ColumnInfo(name = "email") val email: String? = "",
    @ColumnInfo(name = "gender") val gender: Gender? = Gender.FEMALE, // "male", "female", etc.
    @ColumnInfo(name = "roleId") val roleId: Long, // ref to Role table or enum
    @ColumnInfo(name = "profile_photo") val profilePhoto: String? = "", // uri or url
    @ColumnInfo(name = "national_code") val nationalCode: String? = "",
    @ColumnInfo(name = "address") val address: String? = ""
)
