package com.example.delta.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class UnitWithDang(
    @Embedded val unit: Units,
    @ColumnInfo(name = "dang") val dang: Double
)
