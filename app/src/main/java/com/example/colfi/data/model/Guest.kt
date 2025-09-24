// Guest.kt
package com.example.colfi.data.model

data class Guest(
    override val username: String = "guest",
    override val displayName: String = "Guest",
    override val email: String = "guest@local",
    override val role: String = "guest"
) : User {
    // No-arg constructor for Firebase
    constructor() : this("guest", "Guest", "guest@local", "guest")
}