package com.example.delta.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import com.example.delta.enums.CalculateMethod
import com.example.delta.enums.FundFlag
import com.example.delta.enums.PaymentLevel
import com.example.delta.enums.Period
import com.example.delta.enums.Responsible
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "costs",
    foreignKeys = [
        ForeignKey(
            entity = Buildings::class,
            parentColumns = ["buildingId"],
            childColumns = ["buildingId"],
            onDelete = CASCADE
        )
    ]
)
data class Costs(
    @PrimaryKey(autoGenerate = true) val costId: Long = 0,
    @ColumnInfo(name = "buildingId") val buildingId: Long?=null, // Foreign key reference
    @ColumnInfo(name = "cost_name") val costName: String,
    @ColumnInfo(name = "temp_amount") val tempAmount: Double,
    @ColumnInfo(name = "period") val period: Period,//weekly , monthly , yearly
    @ColumnInfo(name = "calculate_method") val calculateMethod: CalculateMethod, //fixed, automatic
    @ColumnInfo(name = "payment_level") val paymentLevel: PaymentLevel, // units, building
    @ColumnInfo(name = "responsible") val responsible: Responsible, // owner, tenant
    @ColumnInfo(name = "fund_flag") var fundFlag: FundFlag,
    @ColumnInfo(name = "charge_flag") var chargeFlag: Boolean?= false
) : Parcelable
