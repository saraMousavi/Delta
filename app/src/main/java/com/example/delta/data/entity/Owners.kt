package com.example.delta.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "owners"
)
data class Owners(
    @PrimaryKey(autoGenerate = true) val ownerId: Long = 0,
    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "last_name") val lastName: String,
    @ColumnInfo(name = "phone_number") val phoneNumber: String,
    @ColumnInfo(name = "mobile_number") val mobileNumber: String,
    @ColumnInfo(name = "birthday") val birthday: String,
    @ColumnInfo(name = "address") val address: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "excel_units_number") val excelUnitsNumber: String? = "",
    @ColumnInfo(name = "excel_building_name") val excelBuildingName: String? = "",
    @ColumnInfo(name = "excel_is_manager") val excelIsManager: Boolean? = false,
    @ColumnInfo(name = "excel_dang") val excelDang: Double? = 0.0
) : Parcelable