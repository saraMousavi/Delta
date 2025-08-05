package com.example.delta.enums

import androidx.annotation.StringRes
import com.example.delta.R

enum class Responsible(@StringRes val resId: Int) {
    OWNER(R.string.owners),
    TENANT(R.string.tenants),
    ALL(R.string.ownerAndTenant);

    fun getDisplayName(context: android.content.Context): String = context.getString(resId)
}