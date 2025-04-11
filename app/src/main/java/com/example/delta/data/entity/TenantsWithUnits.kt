package com.example.delta.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class TenantsWithUnits(
    @Embedded val tenant: Tenants,
    @Relation(
        parentColumn = "tenantId",
        entityColumn = "unitId",
        associateBy = Junction(TenantsUnitsCrossRef::class)
    )
    val units: List<Units>
)

