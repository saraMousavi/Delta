package com.example.delta.data.entity

import androidx.room.Entity

@Entity(primaryKeys = ["buildingId", "tenantId"])
data class BuildingTenantCrossRef(
    val buildingId: Long,
    val tenantId: Long
)

