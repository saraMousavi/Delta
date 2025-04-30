package com.example.delta.enums

import androidx.annotation.StringRes
import com.example.delta.R

// AuthorizationFieldEnum.kt
enum class HomePageFields(
    val objectId: Long, // The AuthorizationObject id for BuildingProfileActivity
    @StringRes val fieldNameRes: Int,
    val fieldType: String,
    @StringRes val descriptionRes: Int
) {
    ALL_BUILDING(1L, R.string.all_building, "list", R.string.all_building),
    OWN_BUILDING(1L, R.string.user_building, "list", R.string.user_building),
    ADD_BUILDING(1L, R.string.add_building_btn, "button", R.string.add_building_btn)
}
