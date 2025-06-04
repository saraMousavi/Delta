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
    STREET(3L, R.string.street, "text", R.string.street),
    POST_CODE(3L, R.string.post_code, "text", R.string.post_code),
    PROVINCE(3L,  R.string.province, "text", R.string.province),
    STATE(3L, R.string.state, "text", R.string.state),
    BUILDING_TYPE(3L, R.string.building_type, "text", R.string.building_type),
    BUILDING_USAGE(3L, R.string.building_usage, "text", R.string.building_usage),
    SHARED_UTILITIES_LIST(3L, R.string.shared_things, "list", R.string.shared_things),
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
    REPORTS_TAB(3L, R.string.reports, "tab", R.string.reports)
    // Add more as needed
}
