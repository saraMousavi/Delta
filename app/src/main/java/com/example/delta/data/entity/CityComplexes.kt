package com.example.delta.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "city_complex")
data class CityComplexes(
    @PrimaryKey(autoGenerate = true) val complexId: Long = 0L,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "address") val address: String?,
    @ColumnInfo(name = "for_building_id") val forBuildingId: Long?=null,
    @ColumnInfo(name = "added_before_create_building") val addedBeforeCreateBuilding: Boolean?= false,
)

