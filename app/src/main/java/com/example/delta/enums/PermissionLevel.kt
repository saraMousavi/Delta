package com.example.delta.enums

enum class PermissionLevel(val value: Int) {
    READ(0),
    WRITE(1),
    DELETE(2),
    FULL(3);

    companion object {
        private val map = entries.associateBy { it.value }
        fun fromValue(value: Int) = map[value] ?: READ
    }
}
