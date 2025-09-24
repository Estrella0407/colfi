package com.example.colfi.ui.viewmodel


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.colfi.data.model.AppDatabase
import com.example.colfi.data.repository.TableRepository


class DineInViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DineInViewModel::class.java)) {
            val db = AppDatabase.getDatabase(context)
            val repository = TableRepository(db.tableDao())
            @Suppress("UNCHECKED_CAST")
            return DineInViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}