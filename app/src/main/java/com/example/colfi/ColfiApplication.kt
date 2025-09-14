// ColfiApplication.kt
package com.example.colfi

import android.app.Application
import com.example.colfi.data.local.AppDatabase
import com.example.colfi.data.repository.CartRepository

class ColfiApplication : Application() {

    // Database instance
    val database by lazy { AppDatabase.getDatabase(this) }

    // Repository instances
    val cartRepository by lazy { CartRepository(database.cartDao()) }
}