// AuthRepository.kt
package com.example.colfi.data.repository

import com.example.colfi.data.model.User
import kotlinx.coroutines.delay

class AuthRepository {
    private val validUsers = mapOf(
        "admin" to User("admin", "Admin User", 200.00, 850, 2),
        "jenny" to User("jenny", "Jenny Chen", 150.55, 721, 1)
    )

    suspend fun login(username: String, password: String): Result<User> {
        return try {
            // Simulate network delay
            delay(1500)

            val validPasswords = mapOf(
                "admin" to "123456",
                "jenny" to "password"
            )

            val user = validUsers[username.lowercase()]
            val validPassword = validPasswords[username.lowercase()]

            if (user != null && validPassword == password) {
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid username or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getGuestUser(): User {
        return User("guest", "Guest", 0.0, 0, 0)
    }
}