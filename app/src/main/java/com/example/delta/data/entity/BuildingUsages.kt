// BuildingUsages.kt
package com.example.delta.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "building_usages")
data class BuildingUsages(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "building_usage_name")
    val buildingUsageName: String
)
