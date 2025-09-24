// User.kt
package com.example.colfi.data.model

// Use interface instead of abstract class for better Firebase compatibility
interface User {
    val username: String
    val displayName: String
    val email: String
    val role: String
}