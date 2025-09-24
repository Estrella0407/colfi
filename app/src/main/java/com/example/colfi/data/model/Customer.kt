// Customer.kt
package com.example.colfi.data.model

data class Customer(
    override val username: String = "",
    override val displayName: String = "",
    override val email: String = "",
    override val role: String = "customer",
    val walletBalance: Double = 0.0,
    val points: Int = 0,
    val vouchers: Int = 0,
    val tier: Int = 0,
    val preferredPaymentMethod: String? = null,
    val deliveryAddresses: List<String> = emptyList(),
    val orderHistory: List<String> = emptyList()
) : User {
    // No-arg constructor for Firebase
    constructor() : this("", "", "", "customer", 0.0, 0, 0, 0)
}