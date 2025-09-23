package com.example.colfi.data.repository


import com.example.colfi.data.model.TableDao
import com.example.colfi.data.model.TableEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow


class TableRepository(private val tableDao: TableDao) {


    // Live updates for UI
    val allTables: Flow<List<TableEntity>> = tableDao.getAllTables()


    // One-time fetch
    suspend fun getAllTablesOnce(): List<TableEntity> = withContext(Dispatchers.IO) {
        tableDao.getAllTablesOnce()
    }


    suspend fun getTable(tableId: String): TableEntity? = withContext(Dispatchers.IO) {
        tableDao.getTable(tableId)
    }


    suspend fun insertAllTables(tables: List<TableEntity>) = withContext(Dispatchers.IO) {
        tableDao.insertAll(tables)
    }


    suspend fun insertTable(table: TableEntity) = withContext(Dispatchers.IO) {
        tableDao.insertTable(table)
    }


    suspend fun updateTable(table: TableEntity) = withContext(Dispatchers.IO) {
        tableDao.updateTable(table)
    }


    suspend fun updateTableStatus(tableId: String, isAvailable: Boolean) = withContext(Dispatchers.IO) {
        val table = tableDao.getTable(tableId)
        if (table != null) {
            tableDao.updateTable(table.copy(isAvailable = isAvailable))
        } else {
            tableDao.insertTable(TableEntity(tableId = tableId, isAvailable = isAvailable))
        }
    }
}