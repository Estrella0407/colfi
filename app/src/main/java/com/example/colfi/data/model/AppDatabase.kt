/*
// AppDatabase.kt
package com.example.colfi.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TableEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tableDao(): TableDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "colfi_database"
                ).build() // Add .fallbackToDestructiveMigration() if needed during development
                INSTANCE = instance
                instance
            }
        }
    }
}
*/
