package com.example.delta.enums

import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import com.example.delta.BuildingFormActivity
import com.example.delta.BuildingProfileActivity
import com.example.delta.BuildingTypeActivity
import com.example.delta.BuildingUsageActivity
import com.example.delta.CostActivity
import com.example.delta.EarningsActivity
import com.example.delta.GuestActivity
import com.example.delta.HomePageActivity
import com.example.delta.R
import com.example.delta.TenantsDetailsActivity

enum class AuthObject(
    val id: Long,
    @StringRes val displayNameRes: Int,
    @StringRes val descriptionRes: Int
) {
    HOMEPAGE(
        id = 1L,
        displayNameRes = R.string.homepage_display,
        descriptionRes = R.string.homepage_desc
    ),
    BUILDING_FORM(
        id = 2L,
        displayNameRes = R.string.building_form_display,
        descriptionRes = R.string.building_form_desc
    ),
    BUILDING_PROFILE(
        id = 3L,
        displayNameRes = R.string.building_profile_display,
        descriptionRes = R.string.building_profile_desc
    ),
    COST_MANAGEMENT(
        id = 4L,
        displayNameRes = R.string.cost_management_display,
        descriptionRes = R.string.cost_management_desc
    ),
    EARNINGS(
        id = 5L,
        displayNameRes = R.string.earning_management_display,
        descriptionRes = R.string.earning_management_desc
    ),
    BUILDING_TYPES(
        id = 6L,
        displayNameRes = R.string.building_type_display,
        descriptionRes = R.string.building_type_desc
    ),
    BUILDING_USAGES(
        id = 7L,
        displayNameRes = R.string.building_usage_display,
        descriptionRes = R.string.building_usage_desc
    ),
    GUEST_ACCESS(
        id = 8L,
        displayNameRes = R.string.guest_display,
        descriptionRes = R.string.guest_desc
    ),
    UNIT_DETAILS(
        id = 9L,
        displayNameRes = R.string.unit_detail_display,
        descriptionRes = R.string.unit_detail_desc
    );

    companion object {
        private val idMap = entries.associateBy { it.id }
        private val nameMap = entries.associateBy { it.name }

        private val activityMap = mapOf<Class<out Activity>, AuthObject>(
            HomePageActivity::class.java to HOMEPAGE,
            BuildingFormActivity::class.java to BUILDING_FORM,
            BuildingProfileActivity::class.java to BUILDING_PROFILE,
            BuildingTypeActivity::class.java to BUILDING_TYPES,
            BuildingUsageActivity::class.java to BUILDING_USAGES,
            CostActivity::class.java to COST_MANAGEMENT,
            EarningsActivity::class.java to EARNINGS,
            GuestActivity::class.java to GUEST_ACCESS,
            TenantsDetailsActivity::class.java to UNIT_DETAILS
        )

        fun fromActivity(activity: Activity): AuthObject? {
            return activityMap[activity::class.java]
        }

        fun fromId(id: Long) = idMap[id]
        fun fromName(name: String) = nameMap[name]
        fun getAll() = entries
    }
    fun getDisplayName(context: Context): String {
        return context.getString(displayNameRes)
    }

    fun getDescription(context: Context): String {
        return context.getString(descriptionRes)
    }
}
