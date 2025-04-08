package com.example.delta.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "tenants"
)
data class Tenants(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "last_name") val lastName: String,
    @ColumnInfo(name = "phone_number") val phoneNumber: Double,
    @ColumnInfo(name = "mobile_number") val mobileNumber: Double,
    @ColumnInfo(name = "work_number") val workNumber: Double,
    @ColumnInfo(name = "birthday") val birthday: String
) : Parcelable