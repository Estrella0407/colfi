// TableRepository.kt
package com.example.colfi.data.repository

import com.example.colfi.data.model.TableDao
import com.example.colfi.data.model.TableEntity
import kotlinx.coroutines.flow.Flow

class TableRepository(private val tableDao: TableDao) {

    val allTables: Flow<List<TableEntity>> = tableDao.getAllTables()

    suspend fun updateTableStatus(tableId: String, isAvailable: Boolean) {
        val table = tableDao.getTable(tableId)
        if (table != null) {
            tableDao.updateTable(table.copy(isAvailable = isAvailable))
        } else {
            tableDao.insertTable(TableEntity(tableId, isAvailable))
        }
    }
}
