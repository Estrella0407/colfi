// MenuRepository.kt
package com.example.colfi.data.repository

import android.util.Log
import com.example.colfi.DrawableMapper
import com.example.colfi.data.model.MenuItem
import com.example.colfi.data.model.CartItem
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
                .get()
                .await()

            val items = snapshot.documents.mapNotNull { document ->
                val menuItem = document.toObject(MenuItem::class.java)
                menuItem?.copy(
                    id = document.id,
                    category = categoryName,
                    // Ensure imageName is set, generate if missing
                    imageName = menuItem.imageName.ifEmpty {
                        DrawableMapper.generateImageName(menuItem.name)
                    }
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
                    .get()
                    .await()

                val items = snapshot.documents.mapNotNull { document ->
                    document.toObject(MenuItem::class.java)?.copy(
                        id = document.id,
                        category = category
                    )
                }
                allItems.addAll(items)
            }

            Result.success(allItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // Update Item
    suspend fun updateMenuItem(menuItem: MenuItem): Result<Unit> {
        return try {
            val updates = mapOf(
                "id" to menuItem.id,
                "name" to menuItem.name,
                "description" to menuItem.description,
                "price" to menuItem.price,
                "category" to menuItem.category,
                "imageName" to menuItem.imageName,
                "availability" to menuItem.availability,
            )

            db.collection("menuCategories")
                .document(menuItem.category.lowercase())
                .collection("items")
                .document(menuItem.id)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get items with low stock
    suspend fun getUnavailableItems(): Result<List<MenuItem>> { // Renamed for clarity
        return try {
            val unavailableItems = mutableListOf<MenuItem>()

            for (category in categories) {
                val snapshot = db.collection("menuCategories")
                    .document(category)
                    .collection("items")
                    .whereEqualTo("availability", false) // Query directly for unavailable items
                    .get()
                    .await()

                snapshot.documents.mapNotNull { document ->
                    document.toObject(MenuItem::class.java)?.copy(
                        id = document.id,
                        category = category,
                        // Ensure imageName is set if needed, similar to other functions
                        imageName = document.toObject(MenuItem::class.java)?.imageName?.ifEmpty {
                            val name = document.toObject(MenuItem::class.java)?.name ?: ""
                            if (name.isNotEmpty()) DrawableMapper.generateImageName(name) else ""
                        } ?: ""
                    )
                }.forEach { menuItem ->
                    unavailableItems.add(menuItem)
                }
            }
            Result.success(unavailableItems)
        } catch (e: Exception) {
            Log.e("MenuRepository", "Error fetching unavailable items: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Check if item has sufficient quantity for order
    suspend fun checkItemAvailability(categoryName: String, itemId: String, requestedQuantity: Int): Result<Boolean> {
        return try {
            val document = db.collection("menuCategories")
                .document(categoryName.lowercase())
                .collection("items")
                .document(itemId)
                .get()
                .await()

            if (document.exists()) {
                val isAvailable = document.getBoolean("availability") ?: false
                Result.success(isAvailable)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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