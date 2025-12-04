package com.example.delta.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.delta.enums.Gender

@Entity(
    tableName = "user"
)
data class User(
    @PrimaryKey(autoGenerate = true) var userId: Long = 0,
    @ColumnInfo(name = "mobile_number") val mobileNumber: String,
    @ColumnInfo(name = "phone_number") val phoneNumber: String? = "",
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "first_name") val firstName: String = "",
    @ColumnInfo(name = "last_name") val lastName: String = "",
    @ColumnInfo(name = "email") val email: String? = "",
    @ColumnInfo(name = "birthday") val birthday: String? = "",
    @ColumnInfo(name = "address") val address: String? = "",
    @ColumnInfo(name = "gender") val gender: Gender? = Gender.FEMALE, // "male", "female", etc.
    @ColumnInfo(name = "profile_photo") val profilePhoto: String? = "", // uri or url
    @ColumnInfo(name = "national_code") val nationalCode: String? = ""
)
