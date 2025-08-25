// User.kt
package com.example.colfi.data.model

data class User(
    val id: String = "", // Firebase document ID
    val username: String = "",
    val displayName: String = "",
    val email: String = "", // Added for Firebase Auth
    val walletBalance: Double = 0.0,
    val points: Int = 0,
    val vouchers: Int = 0
) {
    // Empty constructor for Firebase
    constructor() : this("", "", "", "", 0.0, 0, 0)

    // Convert to map for Firebase
    fun toMap(): Map<String, Any> {
        return mapOf(
            "username" to username,
            "displayName" to displayName,
            "email" to email,
            "walletBalance" to walletBalance,
            "points" to points,
            "vouchers" to vouchers
        )
    }
}