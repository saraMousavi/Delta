package com.example.delta.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class UnitWithTenants(
    @Embedded val unit: Units,
    @Relation(
        parentColumn = "unitId",
        entityColumn = "tenantId",
        associateBy = Junction(
            value = TenantsUnitsCrossRef::class,
            parentColumn = "unitId",
            entityColumn = "tenantId"
        )
    )
    val tenants: List<TenantWithRelation>
)
