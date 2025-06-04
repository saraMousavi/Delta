package com.example.delta.enums

import androidx.annotation.StringRes
import com.example.delta.R

enum class PermissionLevel(val value: Int, @StringRes val labelRes: Int) {
    READ(0, R.string.permission_read),
    WRITE(1, R.string.permission_write),
    DELETE(2, R.string.permission_delete),
    FULL(3, R.string.permission_full);

    companion object {
        private val map = entries.associateBy { it.value }
        fun fromValue(value: Int) = map[value] ?: READ
    }
}

