// Staff.kt
package com.example.colfi.data.model

data class Staff(
    override val username: String = "",
    override val displayName: String = "",
    override val email: String = "",
    override val role: String = "staff",
    val position: String = "",
    val specialty: String? = null,
    val staffId: String = "",
    val staffSince: String = "",
) : User {
    // No-arg constructor for Firebase
    constructor() : this("", "", "", "staff", "", null, "", "")
}