package com.example.delta.data.entity

data class OwnerTabItem(val title: String, val type: OwnerTabType)

enum class OwnerTabType {
    OVERVIEW, FINANCIALS
}
