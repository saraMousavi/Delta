// BuildingTypes.kt
package com.example.delta.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "building_types")
data class BuildingTypes(
    @PrimaryKey(autoGenerate = true) val buildingTypeId: Long = 0,
    @ColumnInfo(name = "building_type_name")
    val buildingTypeName: String
)
