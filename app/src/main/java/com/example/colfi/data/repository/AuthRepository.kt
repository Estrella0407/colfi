// AuthRepository.kt
package com.example.colfi.data.repository

import com.example.colfi.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun login(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                // Authenticate with Firebase Auth
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    // Get user data from Firestore
                    val userDoc = usersCollection.document(firebaseUser.uid).get().await()

                    if (userDoc.exists()) {
                        val user = userDoc.toObject(User::class.java)?.copy(id = firebaseUser.uid)
                        if (user != null) {
                            Result.success(user)
                        } else {
                            Result.failure(Exception("Failed to parse user data"))
                        }
                    } else {
                        val newUser = User(
                            id = firebaseUser.uid,
                            username = firebaseUser.email?.substringBefore("@") ?: "user",
                            displayName = firebaseUser.displayName ?: "User",
                            email = firebaseUser.email ?: "",
                            walletBalance = 150.55,
                            points = 0,
                            vouchers = 0
                        )
                        usersCollection.document(firebaseUser.uid).set(newUser.toMap()).await()
                        Result.success(newUser)
                    }
                } else {
                    Result.failure(Exception("Authentication failed"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun register(email: String, password: String, username: String, displayName: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                // Create Firebase Auth user
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    // Create user profile in Firestore
                    val user = User(
                        id = firebaseUser.uid,
                        username = username,
                        displayName = displayName,
                        email = email,
                        walletBalance = 150.55, // Default values
                        points = 0,
                        vouchers = 0
                    )

                    usersCollection.document(firebaseUser.uid).set(user.toMap()).await()
                    Result.success(user)
                } else {
                    Result.failure(Exception("Failed to create user"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getCurrentUser(): User? {
        return withContext(Dispatchers.IO) {
            try {
                val firebaseUser = auth.currentUser
                if (firebaseUser != null) {
                    val userDoc = usersCollection.document(firebaseUser.uid).get().await()
                    userDoc.toObject(User::class.java)?.copy(id = firebaseUser.uid)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun updateUserProfile(user: User): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                usersCollection.document(user.id).set(user.toMap()).await()
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateWalletBalance(userId: String, newBalance: Double): Result<Double> {
        return withContext(Dispatchers.IO) {
            try {
                usersCollection.document(userId)
                    .update("walletBalance", newBalance).await()
                Result.success(newBalance)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updatePoints(userId: String, newPoints: Int): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                usersCollection.document(userId)
                    .update("points", newPoints).await()
                Result.success(newPoints)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getGuestUser(): User {
        return User("guest", "guest", "Guest", "", 0.0, 0, 0)
    }

}