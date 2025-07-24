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
    @PrimaryKey(autoGenerate = true) val tenantId: Long = 0,
    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "last_name") val lastName: String,
    @ColumnInfo(name = "phone_number") val phoneNumber: String,
    @ColumnInfo(name = "mobile_number") val mobileNumber: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "birthday") val birthday: String,
    @ColumnInfo(name = "number_of_tenants") val numberOfTenants: String,
    @ColumnInfo(name = "start_date") val startDate: String,
    @ColumnInfo(name = "end_date") val endDate: String,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "excel_units_number") val excelUnitsNumber: String? = "",
    @ColumnInfo(name = "excel_building_name") val excelBuildingName: String? = ""

) : Parcelable