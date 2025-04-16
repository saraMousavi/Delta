package com.example.delta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.entity.User
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.launch

class UserViewModel (application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val usersDao = database.usersDao()


    fun insertUser(users: User) = viewModelScope.launch {
        usersDao.insertUser(users)
    }

    fun deleteUser(users: User) = viewModelScope.launch {
        usersDao.deleteUser(users)
    }


    suspend fun getUsers(): List<User> {
        return usersDao.getUsers()
    }

}
