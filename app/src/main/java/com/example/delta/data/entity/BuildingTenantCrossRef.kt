package com.example.delta.data.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(primaryKeys = ["buildingId", "tenantId"],
    indices = [Index("tenantId")] // Add this line
    , tableName = "building_tenant_cross_ref")
data class BuildingTenantCrossRef(
    val buildingId: Long,
    val tenantId: Long
)

