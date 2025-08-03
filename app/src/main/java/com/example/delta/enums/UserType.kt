package com.example.delta.enums

data class UserWithUnit(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val unitNumber: String?,
    val userType: UserType
)

enum class UserType {
    OWNER,
    TENANT
}