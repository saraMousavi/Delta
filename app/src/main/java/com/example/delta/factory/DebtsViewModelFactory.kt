package com.example.delta.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.delta.viewmodel.DebtsViewModel

class DebtsViewModelFactory (private val application: Application) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DebtsViewModel(application) as T
    }
}