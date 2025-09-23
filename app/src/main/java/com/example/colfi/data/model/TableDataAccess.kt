package com.example.colfi.data.model


import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface TableDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tables: List<TableEntity>)


    @Query("SELECT * FROM tables")
    fun getAllTables(): Flow<List<TableEntity>>


    @Query("SELECT * FROM tables")
    suspend fun getAllTablesOnce(): List<TableEntity>


    @Query("SELECT * FROM tables WHERE tableId = :tableId LIMIT 1")
    suspend fun getTable(tableId: String): TableEntity?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTable(table: TableEntity)


    @Update
    suspend fun updateTable(table: TableEntity)
}