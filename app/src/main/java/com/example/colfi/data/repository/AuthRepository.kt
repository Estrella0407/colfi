// AuthRepository.kt
package com.example.colfi.data.repository

import android.util.Log
import com.example.colfi.data.model.User // Make sure your User data class is correctly defined
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentAuthUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Fetches the User data class object from Firestore using UID
    suspend fun getUserDataByUid(uid: String): User? {
        return try {
            val documentSnapshot = usersCollection.document(uid).get().await()
            if (documentSnapshot.exists()) {
                documentSnapshot.toObject(User::class.java)
            } else {
                Log.w("AuthRepository", "No user document found in Firestore for UID: $uid")
                null
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error fetching user data for UID $uid from Firestore", e)
            null
        }
    }

    suspend fun getCurrentUser(): Result<User> {
        val currentFirebaseUser = auth.currentUser // Get the FirebaseUser object
        if (currentFirebaseUser != null) {
            // User is logged in with Firebase Auth, now get their custom data from Firestore
            val uid = currentFirebaseUser.uid
            return try {
                val user: User? = getUserDataByUid(uid) // Reuse your existing function
                if (user != null) {
                    Log.d("AuthRepository", "Successfully fetched custom User data for UID: $uid")
                    Result.success(user)
                } else {
                    Log.w("AuthRepository", "User is authenticated (UID: $uid) but no custom user data found in Firestore.")
                    Result.failure(Exception("User data not found in database for UID: $uid"))
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error occurred while fetching custom User data for UID $uid", e)
                Result.failure(Exception("Failed to retrieve user profile: ${e.message}", e))
            }
        } else {
            // No user is logged in with Firebase Auth
            Log.d("AuthRepository", "No user currently authenticated with Firebase.")
            return Result.failure(Exception("No user logged in."))
        }
    }
    // Function for anonymous login (Guest)
    // Guest data is stored in Firestore keyed by their anonymous UID
    suspend fun loginAsGuest(): Result<Pair<String, User>> { // Returns UID and User object
        return try {
            val authResult = auth.signInAnonymously().await()
            val firebaseUser = authResult.user ?: throw Exception("Anonymous sign-in failed unexpectedly.")

            // Check if guest document already exists, if not create it
            val guestDocRef = usersCollection.document(firebaseUser.uid)
            var guestUser = guestDocRef.get().await().toObject(User::class.java)

            if (guestUser == null) {
                guestUser = User(
                    // id = firebaseUser.uid, // Store UID if your User model has an id field
                    username = "guest_${firebaseUser.uid.take(6)}",
                    displayName = "Guest",
                    email = "",
                    role = "guest",
                    points = 0,
                    vouchers = 0
                )
                guestDocRef.set(guestUser).await()
                Log.d("AuthRepository", "Created Firestore document for new guest UID: ${firebaseUser.uid}")
            } else {
                Log.d("AuthRepository", "Existing Firestore document found for guest UID: ${firebaseUser.uid}")
            }

            Log.d("AuthRepository", "Anonymous login successful for UID: ${firebaseUser.uid}")
            Result.success(Pair(firebaseUser.uid, guestUser))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Anonymous login failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Login with username/password
    // Returns Pair of UID and User object on success
    suspend fun login(usernameInput: String, passwordInput: String): Result<Pair<String, User>> {
        return try {
            Log.d("AuthRepository", "Attempting login for username: $usernameInput")
            val querySnapshot = usersCollection
                .whereEqualTo("username", usernameInput) // Ensure 'username' field exists and is indexed in Firestore
                .limit(1)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                Log.w("AuthRepository", "Username '$usernameInput' not found in Firestore.")
                throw Exception("Invalid username or password") // More generic error
            }

            val userDocSnapshot = querySnapshot.documents[0]
            val email = userDocSnapshot.getString("email")
            if (email == null || email.isBlank()) {
                Log.e("AuthRepository", "Email is null or blank for username '$usernameInput' in Firestore.")
                throw Exception("User data configuration error.")
            }

            Log.d("AuthRepository", "Found email '$email' for username '$usernameInput'. Attempting Firebase Auth sign-in.")
            val authResult = auth.signInWithEmailAndPassword(email, passwordInput).await()
            val firebaseUser = authResult.user ?: throw Exception("Firebase Authentication failed after email/password check.")

            // Fetch the complete User object from Firestore using the now confirmed UID
            val user = userDocSnapshot.toObject(User::class.java) // UID is the document ID
                ?: throw Exception("Error converting user data from Firestore for UID: ${firebaseUser.uid}")

            Log.d("AuthRepository", "Login successful for UID: ${firebaseUser.uid}, DisplayName: ${user.displayName}")
            Result.success(Pair(firebaseUser.uid, user))

        } catch (e: Exception) {
            Log.e("AuthRepository", "Login failed: ${e.message}", e)
            Result.failure(e) // Forward the more specific or generic error
        }
    }


    suspend fun registerUser(
        username: String,
        email: String,
        password: String,
        displayName: String,
        role: String // e.g., "customer", "staff"
    ): Result<FirebaseUser> { // Return FirebaseUser to get UID easily
        return try {
            // Step 1: Check if username already exists
            val usernameExists = usersCollection.whereEqualTo("username", username).limit(1).get().await()
            if (!usernameExists.isEmpty) {
                throw Exception("Username '$username' already taken.")
            }
            // Step 2: Check if email already exists (Firebase Auth will also do this, but good to check early)
            // val emailExists = usersCollection.whereEqualTo("email", email).limit(1).get().await()
            // if (!emailExists.isEmpty) {
            // throw Exception("Email '$email' already registered.")
            // }


            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Firebase user creation failed unexpectedly.")

            // Update Firebase Auth profile display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            // Save comprehensive user data to Firestore, keyed by UID
            val newUser = User(
                // id = firebaseUser.uid, // If your User model has an 'id' field for the UID
                username = username,
                displayName = displayName,
                email = email,
                role = role,
                points = 0,          // Default initial points
                vouchers = 0         // Default initial vouchers
            )
            usersCollection.document(firebaseUser.uid).set(newUser).await()

            Log.d("AuthRepository", "Registration successful for UID: ${firebaseUser.uid}")
            Result.success(firebaseUser)
        } catch (e: Exception) {
            // Firebase Auth throws specific exceptions like FirebaseAuthUserCollisionException for existing email
            Log.e("AuthRepository", "Registration failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun logoutUser() {
        auth.signOut()
        Log.d("AuthRepository", "User logged out")
    }

    // This is a local placeholder. For a real guest flow tied to Firestore, use loginAsGuest()
    // and fetch the User object from the result.
    @Deprecated("Use loginAsGuest() and its result, or fetch user data by guest UID if needed.", ReplaceWith("loginAsGuest()"))
    fun getLocalPlaceholderGuestUser(): User {
        return User(
            username = "guest_local",
            displayName = "Guest (Local)",
            role = "guest"
        )
    }
}
