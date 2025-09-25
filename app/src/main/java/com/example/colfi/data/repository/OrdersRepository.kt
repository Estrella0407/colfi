// OrdersRepository.kt
package com.example.colfi.data.repository

import android.util.Log
import com.example.colfi.data.model.OrderHistory
import com.example.colfi.data.model.OrderItem
import com.example.colfi.data.model.CartItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class OrdersRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usersCollection = db.collection("users")
    private val ordersCollection = db.collection("orders")
    // Method for customers to place orders
    companion object {
        const val PAYMENT_METHOD_WALLET = "Colfi Wallet"
    }

    // OrdersRepository.kt
    suspend fun getCurrentOrders(userId: String? = null): Result<List<OrderHistory>> {
        return try {
            val query = if (userId != null) {
                // For authenticated users, get their orders
                db.collection("orders")
                    .whereEqualTo("customerId", userId)
                    .whereIn("orderStatus", listOf("pending", "preparing", "ready", "delivering"))
            } else {
                // For guest users, get orders with empty customerId
                db.collection("orders")
                    .whereEqualTo("customerId", "")
                    .whereIn("orderStatus", listOf("pending", "preparing", "ready", "delivering"))
            }

            val snapshot = query
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

    suspend fun getOrderHistory(userId: String? = null): Result<List<OrderHistory>> {
        return try {
            val query = if (userId != null) {
                // For authenticated users, get their orders
                db.collection("orders")
                    .whereEqualTo("customerId", userId)
                    .whereIn("orderStatus", listOf("completed", "cancelled"))
            } else {
                // For guest users, get orders with empty customerId
                db.collection("orders")
                    .whereEqualTo("customerId", "")
                    .whereIn("orderStatus", listOf("completed", "cancelled"))
            }

            val snapshot = query
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

    suspend fun placeOrder(
        customerId: String,
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
            val currentUser = auth.currentUser

            // Create order items and calculate total
            val orderItems = cartItems.map { cartItem ->
                OrderItem(
                    id = cartItem.menuItem.id,
                    name = cartItem.menuItem.name,
                    quantity = cartItem.quantity,
                    price = cartItem.menuItem.price
                )
            }

            val totalAmount = cartItems.sumOf { it.totalPrice }
            val estimatedTime = when (orderType) {
                "dine_in" -> 15
                "pick_up" -> 10
                "delivery" -> 30
                else -> 15
            }

            // Create order object
            val orderHistory = OrderHistory(
                customerId = customerId,
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

            Log.d("OrdersRepository", "Placing order with payment method: $paymentMethod")

            // Handle wallet payment
            if (paymentMethod.equals(PAYMENT_METHOD_WALLET, ignoreCase = true)) {
                processWalletPayment(currentUser, customerId, totalAmount, orderHistory)
            } else {
                // Regular payment
                Log.d("OrdersRepository", "Processing regular payment...")
                val documentRef = ordersCollection.add(orderHistory).await()
                Log.d("OrdersRepository", "Regular payment successful. Order ID: ${documentRef.id}")
                Result.success(documentRef.id)
            }

        } catch (e: Exception) {
            Log.e("OrdersRepository", "Error placing order: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun processWalletPayment(
        currentUser: FirebaseUser?,
        customerId: String,
        totalAmount: Double,
        orderHistory: OrderHistory
    ): Result<String> {

        // Validate for wallet payment
        if (currentUser == null) {
            return Result.failure(Exception("User must be logged in for wallet payments"))
        }

        if (customerId != currentUser.uid) {
            return Result.failure(Exception("Order customer ID must match logged-in user for wallet payments"))
        }

        if (totalAmount <= 0) {
            return Result.failure(Exception("Order total amount must be positive"))
        }

        val userDocRef = usersCollection.document(customerId)
        val newOrderDocRef = ordersCollection.document()

        db.runTransaction { transaction ->
            val userSnapshot = transaction.get(userDocRef)
            if (!userSnapshot.exists()) {
                throw Exception("User wallet not found")
            }

            val currentBalance = userSnapshot.getDouble("walletBalance") ?: 0.0
            if (currentBalance < totalAmount) {
                throw Exception("Insufficient wallet balance. Current: RM$currentBalance, Required: RM$totalAmount")
            }

            val newBalance = currentBalance - totalAmount
            transaction.update(userDocRef, "walletBalance", newBalance)
            transaction.set(newOrderDocRef, orderHistory)
            null
        }.await()

        Log.d("OrdersRepository", "Wallet payment successful. Order ID: ${newOrderDocRef.id}")
        return Result.success(newOrderDocRef.id)
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