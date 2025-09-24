package com.example.colfi.data.model


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Database(entities = [TableEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tableDao(): TableDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null


        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "colfi_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
// Use the built instance to pre-populate
                            CoroutineScope(Dispatchers.IO).launch {
// Wait until INSTANCE is set (we set it right after build below)
                                INSTANCE?.tableDao()?.insertAll(
                                    (1..9).map { i ->
                                        TableEntity(
                                            tableId = "T$i",
                                            isAvailable = true
                                        )
                                    }
                                )
                            }
                        }
                    })
                    .build()


                INSTANCE = instance
                instance
            }
        }
    }
}