// CartRepository.kt
package com.example.colfi.data.repository

import android.util.Log
import com.example.colfi.data.local.CartDao
import com.example.colfi.data.local.CartItemEntity
import com.example.colfi.data.model.CartItem
import com.example.colfi.data.model.toCartItem
import com.example.colfi.data.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach

class CartRepository(private val cartDao: CartDao) {

    fun getAllCartItems(): Flow<List<CartItem>> {
        Log.d("CartRepository", "getAllCartItems CALLED") // Log when the method is called
        return cartDao.getAllCartItems()
            .map { entities ->
                Log.d("CartRepository", "DAO emitted ${entities.size} entities") // Log DAO emissions
                entities.map { it.toCartItem() }
            }
            .onEach { items ->
                Log.d("CartRepository", "Mapped to ${items.size} CartItems") // Log after mapping
            }
            .catch { e ->
                Log.e("CartRepository", "Error in getAllCartItems flow", e) // Log errors
                emit(emptyList())
            }
    }


    suspend fun addToCart(cartItem: CartItem): Result<Unit> {
        return try {
            Log.d("CartRepository", "addToCart CALLED with: ${cartItem.menuItem.name}, Qty: ${cartItem.quantity}")
            val existingItem = cartDao.findSimilarItem(cartItem.menuItem.id
            )
            if (existingItem != null) {
                val updatedItem = existingItem.copy(
                    quantity = existingItem.quantity + cartItem.quantity
                )
                Log.d("CartRepository", "Updating existing item: ${updatedItem.menuItemId} to Qty: ${updatedItem.quantity}")
                cartDao.updateCartItem(updatedItem)
            } else {
                Log.d("CartRepository", "Inserting new item: ${cartItem.menuItem.name}")
                cartDao.insertCartItem(cartItem.toEntity())
            }
            Log.d("CartRepository", "addToCart SUCCEEDED")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CartRepository", "addToCart FAILED", e)
            Result.failure(e)
        }
    }

    suspend fun getCartItemWithId(menuItemId: String): CartItemEntity? {
        Log.d("CartRepository", "getCartItemWithId CALLED for: $menuItemId")
        return try {
            val item = cartDao.findSimilarItem(menuItemId)
            Log.d("CartRepository", "getCartItemWithId FOUND: ${item != null}")
            item
        } catch (e: Exception) {
            Log.e("CartRepository", "getCartItemWithId FAILED", e)
            null
        }
    }

    suspend fun removeFromCart(cartItemId: Long): Result<Unit> { // Assuming cartItemId is CartItemEntity's primary key
        return try {
            Log.d("CartRepository", "removeFromCart CALLED for ID: $cartItemId")
            cartDao.getCartItemById(cartItemId)?.let { item ->
                Log.d("CartRepository", "Deleting item: ${item.menuItemName}")
                cartDao.deleteCartItem(item)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CartRepository", "removeFromCart FAILED", e)
            Result.failure(e)
        }
    }

    suspend fun updateQuantity(cartItemId: Long, quantity: Int): Result<Unit> { // Assuming cartItemId is CartItemEntity's primary key
        return try {
            Log.d("CartRepository", "updateQuantity CALLED for ID: $cartItemId to Qty: $quantity")
            if (quantity <= 0) {
                removeFromCart(cartItemId)
            } else {
                cartDao.updateQuantity(cartItemId, quantity)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CartRepository", "updateQuantity FAILED", e)
            Result.failure(e)
        }
    }


    suspend fun clearCart(): Result<Unit> {
        return try {
            Log.d("CartRepository", "clearCart CALLED")
            cartDao.clearCart()
            Log.d("CartRepository", "clearCart SUCCEEDED")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CartRepository", "clearCart FAILED", e)
            Result.failure(e)
        }
    }

    fun getTotalItemCount(): Flow<Int> {
        return cartDao.getTotalItemCount()
            .map { (it ?: 0L).toInt() }
            .catch { emit(0) }
    }

    fun getTotalPrice(): Flow<Double> {
        return cartDao.getTotalPrice()
            .map { it ?: 0.0 }
            .catch { emit(0.0) }
    }
}