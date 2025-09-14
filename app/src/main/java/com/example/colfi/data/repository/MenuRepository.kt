// MenuRepository.kt
package com.example.colfi.data.repository

import com.example.colfi.data.model.MenuItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MenuRepository () {
    private val db = FirebaseFirestore.getInstance()

    private val categories = listOf("coffee", "non-coffee", "tea", "add-on")

    suspend fun getMenuItemsByCategory(categoryName: String): Result<List<MenuItem>> {
        return try {
            val snapshot = db.collection("menuCategories")
                .document(categoryName.lowercase())
                .collection("items")
                .whereEqualTo("availability", true) // This matches your Firestore field
                .get()
                .await()

            val items = snapshot.documents.mapNotNull { document ->
                document.toObject(MenuItem::class.java)?.copy(
                    id = document.id,
                    category = categoryName // Set the category for each item
                )
            }

            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllMenuItems(): Result<List<MenuItem>> {
        return try {
            val allItems = mutableListOf<MenuItem>()

            for (category in categories) {
                val snapshot = db.collection("menuCategories")
                    .document(category)
                    .collection("items")
                    .whereEqualTo("availability", true) // This matches your Firestore field
                    .get()
                    .await()

                val items = snapshot.documents.mapNotNull { document ->
                    document.toObject(MenuItem::class.java)?.copy(
                        id = document.id,
                        category = category // Set the category for each item
                    )
                }
                allItems.addAll(items)
            }

            Result.success(allItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Add method to get single menu item
    suspend fun getMenuItemById(categoryName: String, itemId: String): Result<MenuItem?> {
        return try {
            val document = db.collection("menuCategories")
                .document(categoryName.lowercase())
                .collection("items")
                .document(itemId)
                .get()
                .await()

            val item = if (document.exists()) {
                document.toObject(MenuItem::class.java)?.copy(
                    id = document.id,
                    category = categoryName
                )
            } else null

            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Add method to search menu items
    suspend fun searchMenuItems(query: String): Result<List<MenuItem>> {
        return try {
            val allItems = mutableListOf<MenuItem>()

            for (category in categories) {
                val snapshot = db.collection("menuCategories")
                    .document(category)
                    .collection("items")
                    .whereEqualTo("availability", true)
                    .get()
                    .await()

                val items = snapshot.documents.mapNotNull { document ->
                    document.toObject(MenuItem::class.java)?.copy(
                        id = document.id,
                        category = category
                    )
                }.filter { item ->
                    // Simple search by name (case-insensitive)
                    item.name.contains(query, ignoreCase = true) ||
                            item.description.contains(query, ignoreCase = true)
                }

                allItems.addAll(items)
            }

            Result.success(allItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCategories(): List<String> = categories

    fun getCategoryDisplayName(categoryName: String): String {
        return when (categoryName.lowercase()) {
            "coffee" -> "Coffee"
            "non-coffee" -> "Non-coffee"
            "tea" -> "Tea"
            "add-on" -> "Add On"
            else -> categoryName.replaceFirstChar { it.uppercase() }
        }
    }
}