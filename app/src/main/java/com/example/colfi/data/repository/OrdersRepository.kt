// OrdersRepository.kt
package com.example.colfi.data.repository

import com.example.colfi.data.model.OrderHistory
import com.example.colfi.data.model.OrderItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class OrdersRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getCurrentOrders(): Result<List<OrderHistory>> {
        return try {
            val snapshot = db.collection("orders")
                .whereIn("orderStatus", listOf("pending", "preparing", "ready", "delivering"))
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { document ->
                document.toObject(OrderHistory::class.java)?.copy(
                    orderId = document.id
                )
            }

            Result.success(orders.sortedByDescending { it.orderDate })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrderHistory(): Result<List<OrderHistory>> {
        return try {
            val snapshot = db.collection("orders")
                .whereIn("orderStatus", listOf("completed", "cancelled"))
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { document ->
                document.toObject(OrderHistory::class.java)?.copy(
                    orderId = document.id
                )
            }

            Result.success(orders.sortedByDescending { it.orderDate })
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
}