package com.example.delta.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.delta.enums.Period
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "earnings"
)
data class Earnings(
    @PrimaryKey(autoGenerate = true) val earningsId: Long = 0,
    @ColumnInfo(name = "earnings_name") val earningsName: String,
    @ColumnInfo(name = "buildingId") val buildingId: Long?= null, // Foreign key reference
    @ColumnInfo(name = "amount") val amount: Double,
    @ColumnInfo(name = "period") val period: Period,//weekly , monthly , yearly
    @ColumnInfo(name = "invoice_flag") var invoiceFlag: Boolean?= false,
    @ColumnInfo(name = "due_date") val dueDate: String, // Due date of the debt
    ) : Parcelable
