// File: com/example/colfi/ui/viewmodel/DineInViewModelFactory.kt
package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.colfi.data.repository.TableRepository

class DineInViewModelFactory(
    private val tableRepository: TableRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DineInViewModel::class.java)) {
            return DineInViewModel(tableRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}