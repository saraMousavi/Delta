package com.example.delta.enums

enum class FundFlag(val value: Int) {
    HAD_EFFECT(1),
    NO_EFFECT(-1),
    IN_EFFECTIVE(0);

    companion object {
        private val map = entries.associateBy { it.value }
        fun fromValue(value: Int) = map[value] ?: IN_EFFECTIVE
    }
}
