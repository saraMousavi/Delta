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
    UNITS_TAB(3L, R.string.units, "tab", R.string.units),
    TENANTS_TAB(3L, R.string.tenants, "tab", R.string.tenants),
    REPORTS_TAB(3L, R.string.reports, "tab", R.string.reports)
    // Add more as needed
}
