// CartRepository.kt
package com.example.colfi.data.repository

import com.example.colfi.data.local.CartDao
import com.example.colfi.data.local.CartItemEntity
import com.example.colfi.data.model.CartItem
import com.example.colfi.data.model.toCartItem
import com.example.colfi.data.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch

class CartRepository(private val cartDao: CartDao) {

    fun getAllCartItems(): Flow<List<CartItem>> {
        return cartDao.getAllCartItems()
            .map { entities -> entities.map { it.toCartItem() } }
            .catch { emit(emptyList()) }
    }

    suspend fun addToCart(cartItem: CartItem): Result<Unit> {
        return try {
            // Check if similar item exists
            val existingItem = cartDao.findSimilarItem(
                cartItem.menuItem.id,
                cartItem.selectedTemperature,
                cartItem.selectedSugarLevel
            )

            if (existingItem != null) {
                val updatedItem = existingItem.copy(
                    quantity = existingItem.quantity + cartItem.quantity
                )
                cartDao.updateCartItem(updatedItem)
            } else {
                cartDao.insertCartItem(cartItem.toEntity())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromCart(cartItemId: Long): Result<Unit> {
        return try {
            cartDao.getCartItemById(cartItemId)?.let { item ->
                cartDao.deleteCartItem(item)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateQuantity(cartItemId: Long, quantity: Int): Result<Unit> {
        return try {
            if (quantity <= 0) {
                removeFromCart(cartItemId)
            } else {
                cartDao.updateQuantity(cartItemId, quantity)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearCart(): Result<Unit> {
        return try {
            cartDao.clearCart()
            Result.success(Unit)
        } catch (e: Exception) {
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

    // Fixed method - now returns CartItemEntity instead of just the ID
    suspend fun getCartItemWithId(
        menuItemId: String,
        temperature: String?,
        sugarLevel: String?
    ): CartItemEntity? {
        return try {
            cartDao.findSimilarItem(menuItemId, temperature, sugarLevel)
        } catch (e: Exception) {
            null
        }
    }
}