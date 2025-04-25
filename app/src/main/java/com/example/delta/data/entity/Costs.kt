package com.example.delta.data.entity

import android.os.Parcelable
import androidx.compose.ui.res.stringResource
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.delta.R
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
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "buildingId") val buildingId: Long?=null, // Foreign key reference
    @ColumnInfo(name = "cost_name") val costName: String,
    @ColumnInfo(name = "temp_amount") val tempAmount: Double,
    @ColumnInfo(name = "period") val period: List<String>,//weekly , monthly , yearly
    @ColumnInfo(name = "calculate_method") val calculateMethod: List<String>, //fixed, automatic, area, people
    @ColumnInfo(name = "payment_level") val paymentLevel: List<String>, // units, building
    @ColumnInfo(name = "responsible") val responsible: List<String>, // owner, tenant
    @ColumnInfo(name = "fund_flag") var fundFlag: Boolean = false
) : Parcelable
