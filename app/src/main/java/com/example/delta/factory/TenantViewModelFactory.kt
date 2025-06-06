package com.example.delta.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.delta.viewmodel.TenantViewModel

class TenantViewModelFactory (private val application: Application) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TenantViewModel(application) as T
    }
}