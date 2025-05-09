package com.example.delta.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TenantWithRelation(
    @Embedded val tenant: Tenants,
    @Relation(
        parentColumn = "tenantId",
        entityColumn = "tenantId",
        entity = TenantsUnitsCrossRef::class
    )
    val crossRef: TenantsUnitsCrossRef
)
