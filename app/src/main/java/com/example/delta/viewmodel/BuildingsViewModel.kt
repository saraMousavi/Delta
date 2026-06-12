package com.example.delta.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Earnings
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BuildingsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val showEarningsDialog = mutableStateOf(false)
    val showCapitalCostDialog = mutableStateOf(false)
    val showOperationalCostDialog = mutableStateOf(false)
    val showUnitsDialog = mutableStateOf(false)


    private val _province = MutableStateFlow("تهران")
    val province: StateFlow<String> = _province.asStateFlow()

    private val _state = MutableStateFlow("تهران")
    val state: StateFlow<String> = _state.asStateFlow()


    fun hideDialogs() {
        showEarningsDialog.value = false
        showCapitalCostDialog.value = false
        showOperationalCostDialog.value = false
        showUnitsDialog.value = false
    }



}
