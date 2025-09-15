// User.kt
package com.example.colfi.data.model

data class User(
    val username: String,
    val displayName: String,
    val email: String = "",
    val walletBalance: Double = 150.55,
    val points: Int = 721,
    val vouchers: Int = 1
)