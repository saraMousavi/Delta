package com.example.delta.enums

import androidx.annotation.StringRes
import com.example.delta.R

enum class Roles (@StringRes val resId: Int){
    ADMIN(R.string.admin),
    BUILDING_MANAGER(R.string.building_manager),
    COMPLEX_MANAGER(R.string.complex_manager),
    PROPERTY_OWNER(R.string.owner),
    PROPERTY_TENANT(R.string.tenant),
    INDEPENDENT_USER(R.string.independent_user),
    GUEST_BUILDING_MANAGER(R.string.building_manager),
    GUEST_COMPLEX_MANAGER(R.string.complex_manager),
    GUEST_PROPERTY_OWNER(R.string.owner),
    GUEST_PROPERTY_TENANT(R.string.tenant),
    GUEST_INDEPENDENT_USER(R.string.independent_user);

    fun getDisplayName(context: android.content.Context): String = context.getString(resId)
}