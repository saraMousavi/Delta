package com.example.delta.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class DebtWithUnitName(
    @Embedded val debt: Debts,
    @ColumnInfo(name = "unit_number") val unitNumber: String
)

