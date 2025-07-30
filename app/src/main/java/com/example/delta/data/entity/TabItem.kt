package com.example.delta.data.entity

data class TabItem(val title: String, val type: TabType)

enum class TabType {
    OVERVIEW, OWNERS, UNITS, TENANTS, FUNDS,TRANSACTIONS, PHONEBOOK_TAB
}
