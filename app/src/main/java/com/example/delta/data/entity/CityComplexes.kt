package com.example.delta.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "city_complex")
data class CityComplexes(
    @PrimaryKey(autoGenerate = true) val complexId: Long = 0L,
    val name: String,
    val address: String?
)

