package com.example.delta.enums

import androidx.annotation.StringRes
import com.example.delta.R

// AuthorizationFieldEnum.kt
enum class BuildingFormFields(
    val objectId: Long, // The AuthorizationObject id for BuildingProfileActivity
    @StringRes val fieldNameRes: Int,
    val fieldType: String,
    @StringRes val descriptionRes: Int
) {
    BUILDING_INFO_PAGE(2L, R.string.buildings_info_page, "page", R.string.buildings_info_page),
    UNIT_PAGE(2L, R.string.units_page, "text", R.string.units_page),
    OWNERS_PAGE(2L, R.string.owners_page, "text", R.string.owners_page),
    TENANTS_PAGE(2L,  R.string.tenants_page, "text", R.string.tenants_page),
    COST_PAGE(2L, R.string.cost_page, "text", R.string.cost_page),
}
