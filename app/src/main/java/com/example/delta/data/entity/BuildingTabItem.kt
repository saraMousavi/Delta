package com.example.delta.data.entity

data class BuildingTabItem(val title: String, val type: BuildingTabType)

enum class BuildingTabType {
    OVERVIEW, OWNERS, UNITS, TENANTS, FUNDS,TRANSACTIONS, PHONEBOOK_TAB
}
