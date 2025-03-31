package com.example.delta

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.entity.Income
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class IncomeViewModel (application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val incomesDao = database.incomeDao()


    fun insertIncome(income: Income) = viewModelScope.launch {
        incomesDao.insertIncome(income)
    }

    fun deleteIncome(income: Income) = viewModelScope.launch {
        incomesDao.deleteIncome(income)
    }

    fun getAllIncome(): Flow<List<Income>> {
        return incomesDao.getAllIncome()
    }
}
