// BuildingUsages.kt
package com.example.delta.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "building_usages")
data class BuildingUsages(
    @PrimaryKey(autoGenerate = true) val buildingUsageId: Long = 0,
    @ColumnInfo(name = "building_usage_name")
    val buildingUsageName: String,
    @ColumnInfo(name = "for_building_id") val forBuildingId: Long?=null,
    @ColumnInfo(name = "added_before_create_building") val addedBeforeCreateBuilding: Boolean?= false,
)
