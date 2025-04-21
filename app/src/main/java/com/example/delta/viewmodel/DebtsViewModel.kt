package com.example.delta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.entity.Debts
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DebtsViewModel (application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val debtsDao = database.debtsDao()


    fun insertDebt(debts: Debts) = viewModelScope.launch {
        debtsDao.insertDebt(debts)
    }

    fun deleteDebt(debts: Debts) = viewModelScope.launch {
        debtsDao.deleteDebt(debts)
    }

    fun updateDebt(debts: Debts) = viewModelScope.launch {
        debtsDao.updateDebt(debts)
    }

    fun getAllDebt(): List<Debts> {
        return debtsDao.getAllDebts()
    }


    fun fetchAndProcessDebts(unitId: Long) : Flow<List<Debts>> {
        return debtsDao.getDebtsForUnit(unitId)
    }

    fun fetchAndProcessPays(unitId: Long) : Flow<List<Debts>> {
        return debtsDao.getPaysForUnit(unitId)
    }
}
