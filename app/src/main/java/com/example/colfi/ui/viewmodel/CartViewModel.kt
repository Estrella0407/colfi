package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.colfi.data.model.CartItem

class CartViewModel : ViewModel() {

    private val _cartItems = mutableListOf<CartItem>()
    val cartItems: List<CartItem> get() = _cartItems

    fun addToCart(item: CartItem) {
        // Check if same item + same option already exists
        val existing = _cartItems.find {
            it.menuItem.id == item.menuItem.id && it.option == item.option
        }

        if (existing != null) {
            val updated = existing.copy(quantity = existing.quantity + item.quantity)
            _cartItems[_cartItems.indexOf(existing)] = updated
        } else {
            _cartItems.add(item)
        }
    }

    fun removeFromCart(item: CartItem) {
        _cartItems.remove(item)
    }

    fun clearCart() {
        _cartItems.clear()
    }

    fun getTotalPrice(): Double {
        return _cartItems.sumOf { it.totalPrice }
    }
}
