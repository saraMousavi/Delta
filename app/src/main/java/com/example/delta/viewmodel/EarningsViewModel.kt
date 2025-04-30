package com.example.delta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Earnings
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class EarningsViewModel (application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val earningsDao = database.earningsDao()


    fun insertEarnings(earnings: Earnings) = viewModelScope.launch {
        earningsDao.insertEarnings(earnings)
    }

    fun deleteEarnings(earnings: Earnings) = viewModelScope.launch {
        earningsDao.deleteEarnings(earnings)
    }

    fun getAllEarnings(): Flow<List<Earnings>> {
        return earningsDao.getAllEarnings()
    }

    fun fetchAndProcessEarnings(buildingId: Long) : Flow<List<Earnings>> {
        return earningsDao.getEarningsForBuilding(buildingId)
    }
}
