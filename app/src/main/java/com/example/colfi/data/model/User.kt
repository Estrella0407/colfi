// User.kt
package com.example.colfi.data.model

data class User(
    val username: String = "",
    val displayName: String = "",
    val email: String = "",
    val walletBalance: Double = 0.0,
    val points: Int = 0,
    val vouchers: Int = 0
    )