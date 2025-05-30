package com.example.delta.data.entity

import androidx.room.Embedded

data class BuildingWithTypesAndUsages(
    @Embedded val building: Buildings,
    val roleName: String,
    val buildingTypeName: String,
    val buildingUsageName: String
)
