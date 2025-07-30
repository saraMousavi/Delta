package com.example.delta.enums

import androidx.annotation.StringRes
import com.example.delta.R

// AuthorizationFieldEnum.kt
enum class BuildingProfileFields(
    val objectId: Long, // The AuthorizationObject id for BuildingProfileActivity
    @StringRes val fieldNameRes: Int,
    val fieldType: String,
    @StringRes val descriptionRes: Int
) {
    BUILDING_NAME(3L, R.string.building_name, "text", R.string.building_name),
    DOCUMENTS(3L, R.string.documents, "list", R.string.documents),
    FUNDS_TAB(3L, R.string.funds, "tab", R.string.funds),
    OWNERS_TAB(3L, R.string.owners, "tab", R.string.owners),
    ALL_OWNERS(3L, R.string.all_owners, "list", R.string.all_owners),
    USERS_OWNERS(3L, R.string.users_owners, "list", R.string.users_owners),
    UNITS_TAB(3L, R.string.units, "tab", R.string.units),
    ALL_UNITS(3L, R.string.all_units, "list", R.string.all_units),
    USERS_UNITS(3L, R.string.users_units, "list", R.string.users_units),
    ALL_TENANTS(3L, R.string.all_tenants, "list", R.string.all_tenants),
    USERS_TENANTS(3L, R.string.users_tenant, "list", R.string.users_tenant),
    TENANTS_TAB(3L, R.string.tenants, "tab", R.string.tenants),
    TRANSACTION_TAB(3L, R.string.transaction, "tab", R.string.transaction),
    REPORTS_TAB(3L, R.string.reports, "tab", R.string.reports),
    PHONEBOOK_TAB(3L, R.string.phone_book, "tab", R.string.phone_book)
    // Add more as needed
}
