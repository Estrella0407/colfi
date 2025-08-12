// MenuRepository.kt
package com.example.colfi.data.repository

import com.example.colfi.data.model.MenuItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MenuRepository {
    private val db = FirebaseFirestore.getInstance()

    private val categories = listOf("coffee", "non-coffee", "tea", "add-on")

    suspend fun getMenuItemsByCategory(categoryName: String): Result<List<MenuItem>> {
        return try {
            val snapshot = db.collection("menuCategories")
                .document(categoryName.lowercase())
                .collection("items")
                .whereEqualTo("availability", true)
                .get()
                .await()

            val items = snapshot.documents.mapNotNull { document ->
                document.toObject(MenuItem::class.java)?.copy(
                    id = document.id,
                    categoryID = categoryName
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
                    .whereEqualTo("availability", true)
                    .get()
                    .await()

                val items = snapshot.documents.mapNotNull { document ->
                    document.toObject(MenuItem::class.java)?.copy(
                        id = document.id,
                        categoryID = category
                    )
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