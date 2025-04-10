package com.example.delta.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation


data class BuildingWithOwnersAndTenants(
    @Embedded val building: Buildings,

    @Relation(
        parentColumn = "buildingId",
        entityColumn = "ownerId",
        associateBy = Junction(BuildingOwnerCrossRef::class)
    )
    val owners: List<Owners>,

    @Relation(
        parentColumn = "buildingId",
        entityColumn = "tenantId",
        associateBy = Junction(BuildingTenantCrossRef::class)
    )
    val tenants: List<Tenants>
)


