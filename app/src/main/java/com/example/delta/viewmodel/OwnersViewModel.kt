package com.example.delta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Owners
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class OwnersViewModel (application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val ownersDao = database.ownersDao()


    fun insertOwners(owners: Owners) = viewModelScope.launch {
        ownersDao.insertOwners(owners)
    }

    fun deleteOwners(owners: Owners) = viewModelScope.launch {
        ownersDao.deleteOwners(owners)
    }

    fun getAllOwners(): Flow<List<Owners>> {
        return ownersDao.getAllOwners()
    }
    fun getAllMenuOwners(): Flow<List<Owners>> {
        return ownersDao.getAllMenuOwners()
    }

}
