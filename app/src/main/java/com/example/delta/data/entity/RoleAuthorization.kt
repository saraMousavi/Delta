package com.example.delta.data.entity

data class RoleAuthorization(
    val role: String, // e.g., "owner", "tenant", "admin"
    val authorizationObject: Set<String>, // e.g., "HomeActivity", "ManageUsersActivity", etc.
    val authorizedFields: Map<String, Set<String>>
)
