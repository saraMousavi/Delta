package com.example.delta.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class TenantWithBuildings(
    @Embedded val tenant: Tenants,

    @Relation(
        parentColumn = "tenantId",
        entityColumn = "buildingId",
        associateBy = Junction(BuildingTenantCrossRef::class)
    )
    val buildings: List<Buildings>
)

