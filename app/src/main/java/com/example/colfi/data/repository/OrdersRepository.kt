// OrdersRepository.kt
package com.example.colfi.data.repository

import com.example.colfi.data.model.OrderHistory
import com.example.colfi.data.model.OrderItem
import com.example.colfi.data.model.CartItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class OrdersRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getCurrentOrders(): Result<List<OrderHistory>> {
        return try {
            val snapshot = db.collection("orders")
                .whereIn("orderStatus", listOf("pending", "preparing", "ready", "delivering"))
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { document ->
                document.toObject(OrderHistory::class.java)?.copy(
                    orderId = document.id
                )
            }

            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrderHistory(): Result<List<OrderHistory>> {
        return try {
            val snapshot = db.collection("orders")
                .whereIn("orderStatus", listOf("completed", "cancelled"))
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { document ->
                document.toObject(OrderHistory::class.java)?.copy(
                    orderId = document.id
                )
            }

            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrdersByType(orderType: String): Result<List<OrderHistory>> {
        return try {
            val snapshot = db.collection("orders")
                .whereEqualTo("orderType", orderType)
                .whereIn("orderStatus", listOf("pending", "preparing", "ready", "delivering"))
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { document ->
                document.toObject(OrderHistory::class.java)?.copy(
                    orderId = document.id
                )
            }

            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelOrder(orderId: String): Result<Unit> {
        return try {
            db.collection("orders")
                .document(orderId)
                .update("orderStatus", "cancelled")
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Unit> {
        return try {
            db.collection("orders")
                .document(orderId)
                .update("orderStatus", newStatus)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Method for customers to place orders
    suspend fun placeOrder(
        customerName: String,
        customerPhone: String,
        cartItems: List<CartItem>,
        orderType: String,
        paymentMethod: String,
        deliveryAddress: String? = null,
        tableNumber: String? = null,
        specialInstructions: String = ""
    ): Result<String> {
        return try {
            val orderItems = cartItems.map { cartItem ->
                OrderItem(
                    id = cartItem.menuItem.id,
                    name = cartItem.menuItem.name,
                    quantity = cartItem.quantity,
                    price = cartItem.menuItem.price,
                    temperature = cartItem.selectedTemperature ?: "",
                    customizations = buildList {
                        cartItem.selectedSugarLevel?.let { add(it) }
                    }
                )
            }

            val totalAmount = cartItems.sumOf { it.totalPrice }
            val estimatedTime = when (orderType) {
                "dine_in" -> 15
                "pick_up" -> 10
                "delivery" -> 30
                else -> 15
            }

            val orderHistory = OrderHistory(
                customerName = customerName,
                customerPhone = customerPhone,
                orderItems = orderItems,
                totalAmount = totalAmount,
                orderType = orderType,
                orderStatus = "pending",
                orderDate = System.currentTimeMillis(),
                estimatedTime = estimatedTime,
                specialInstructions = specialInstructions,
                paymentMethod = paymentMethod,
                deliveryAddress = deliveryAddress,
                tableNumber = tableNumber
            )

            val documentRef = db.collection("orders").add(orderHistory).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Real-time listener for staff orders
    fun observeCurrentOrders(onOrdersChanged: (List<OrderHistory>) -> Unit) {
        db.collection("orders")
            .whereIn("orderStatus", listOf("pending", "preparing", "ready", "delivering"))
            .orderBy("orderDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onOrdersChanged(emptyList())
                    return@addSnapshotListener
                }

                val orders = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(OrderHistory::class.java)?.copy(
                        orderId = document.id
                    )
                } ?: emptyList()

                onOrdersChanged(orders)
            }
    }
}