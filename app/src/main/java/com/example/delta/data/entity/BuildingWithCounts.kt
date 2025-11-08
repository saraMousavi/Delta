package com.example.delta.data.entity

data class BuildingWithCounts(
    val buildingId: Long,
    val complexId: Long?,
    val name: String,
    val phone: String,
    val email: String,
    val postCode: String,
    val street: String,
    val province: String,
    val state: String,
    val buildingTypeId: Long?,
    val buildingUsageId: Long?,
    val fund: Double,
    val userId: Long,

    val buildingTypeName: String?,
    val buildingUsageName: String?,

    val unitsCount: Int,
    val ownersCount: Int
)