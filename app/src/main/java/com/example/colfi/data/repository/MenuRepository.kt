// MenuRepository.kt
package com.example.colfi.data.repository

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
                .whereEqualTo("availability", true)
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

    // Add new menu item with automatic image name generation
    suspend fun addMenuItem(menuItem: MenuItem): Result<MenuItem> {
        return try {
            val documentRef = db.collection("menuCategories")
                .document(menuItem.category.lowercase())
                .collection("items")
                .document()

            val itemWithImage = menuItem.copy(
                id = documentRef.id,
                imageName = menuItem.imageName.ifEmpty {
                    DrawableMapper.generateImageName(menuItem.name)
                }
            )

            documentRef.set(itemWithImage).await()
            Result.success(itemWithImage)
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

    // Quantity management methods
    suspend fun updateItemQuantity(categoryName: String, itemId: String, newQuantity: Int): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "quantity" to newQuantity
            )

            // Auto-manage availability based on quantity
            if (newQuantity <= 0) {
                updates["availability"] = false
            } else {
                updates["availability"] = true
            }

            db.collection("menuCategories")
                .document(categoryName.lowercase())
                .collection("items")
                .document(itemId)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun decreaseItemQuantity(categoryName: String, itemId: String, amount: Int): Result<Unit> {
        return try {
            val docRef = db.collection("menuCategories")
                .document(categoryName.lowercase())
                .collection("items")
                .document(itemId)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentQuantity = snapshot.getLong("quantity")?.toInt() ?: 0

                if (currentQuantity >= amount) {
                    val newQuantity = currentQuantity - amount
                    transaction.update(docRef, "quantity", newQuantity)

                    // Auto-disable if quantity reaches 0
                    if (newQuantity <= 0) {
                        transaction.update(docRef, "availability", false)
                    }
                } else {
                    throw Exception("Insufficient quantity. Available: $currentQuantity, Requested: $amount")
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun increaseItemQuantity(categoryName: String, itemId: String, amount: Int): Result<Unit> {
        return try {
            val docRef = db.collection("menuCategories")
                .document(categoryName.lowercase())
                .collection("items")
                .document(itemId)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentQuantity = snapshot.getLong("quantity")?.toInt() ?: 0
                val maxQuantity = snapshot.getLong("maxQuantity")?.toInt() ?: 100

                if (currentQuantity + amount <= maxQuantity) {
                    val newQuantity = currentQuantity + amount
                    transaction.update(docRef, "quantity", newQuantity)

                    // Re-enable if it was disabled due to 0 quantity
                    if (currentQuantity == 0 && amount > 0) {
                        transaction.update(docRef, "availability", true)
                    }
                } else {
                    throw Exception("Exceeds maximum quantity. Max: $maxQuantity, Attempted: ${currentQuantity + amount}")
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get items with low stock
    suspend fun getLowStockItems(): Result<List<MenuItem>> {
        return try {
            val lowStockItems = mutableListOf<MenuItem>()

            for (category in categories) {
                val snapshot = db.collection("menuCategories")
                    .document(category)
                    .collection("items")
                    .get()
                    .await()

                val items = snapshot.documents.mapNotNull { document ->
                    val item = document.toObject(MenuItem::class.java)?.copy(
                        id = document.id,
                        category = category
                    )
                    // Only include items where quantity <= minQuantity
                    if (item != null && item.quantity <= item.minQuantity) {
                        item
                    } else null
                }
                lowStockItems.addAll(items)
            }

            Result.success(lowStockItems)
        } catch (e: Exception) {
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
                val currentQuantity = document.getLong("quantity")?.toInt() ?: 0
                val isAvailable = document.getBoolean("availability") ?: false

                Result.success(isAvailable && currentQuantity >= requestedQuantity)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Process order - decrease quantities for all items in the cart
    suspend fun processOrder(cartItems: List<CartItem>): Result<Unit> {
        return try {
            // Group cart items by menu item ID to handle multiple customizations of same item
            val itemQuantities = mutableMapOf<String, Int>()
            val itemCategories = mutableMapOf<String, String>()

            cartItems.forEach { cartItem ->
                val menuItem = cartItem.menuItem
                itemQuantities[menuItem.id] = (itemQuantities[menuItem.id] ?: 0) + cartItem.quantity
                itemCategories[menuItem.id] = menuItem.category
            }

            // First, check if all items have sufficient quantity
            for ((itemId, totalQuantity) in itemQuantities) {
                val category = itemCategories[itemId] ?: continue
                val availabilityResult = checkItemAvailability(category, itemId, totalQuantity)

                if (availabilityResult.isFailure || !availabilityResult.getOrDefault(false)) {
                    // Find the item name for error message
                    val itemName = cartItems.find { it.menuItem.id == itemId }?.menuItem?.name ?: "Unknown item"
                    return Result.failure(Exception("$itemName is not available in requested quantity ($totalQuantity)"))
                }
            }

            // If all items are available, decrease quantities
            for ((itemId, totalQuantity) in itemQuantities) {
                val category = itemCategories[itemId] ?: continue
                decreaseItemQuantity(category, itemId, totalQuantity)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Batch update quantities (useful for restocking)
    suspend fun batchUpdateQuantities(updates: List<QuantityUpdate>): Result<Unit> {
        return try {
            val batch = db.batch()

            updates.forEach { update ->
                val docRef = db.collection("menuCategories")
                    .document(update.category.lowercase())
                    .collection("items")
                    .document(update.itemId)

                batch.update(docRef, "quantity", update.newQuantity)

                // Update availability based on quantity
                val availability = update.newQuantity > 0
                batch.update(docRef, "availability", availability)
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Existing methods
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

// Helper data class for batch updates
data class QuantityUpdate(
    val category: String,
    val itemId: String,
    val newQuantity: Int
)