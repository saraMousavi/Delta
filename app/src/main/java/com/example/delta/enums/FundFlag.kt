package com.example.delta.enums

enum class FundFlag(val value: Int) {
    POSITIVE_EFFECT(1),
    NEGATIVE_EFFECT(-1),
    NO_EFFECT(0);

    companion object {
        private val map = entries.associateBy { it.value }
        fun fromValue(value: Int) = map[value] ?: NO_EFFECT
    }
}
