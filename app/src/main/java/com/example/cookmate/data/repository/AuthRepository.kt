package com.example.cookmate.data.repository

import com.example.cookmate.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository class that handles authentication operations
 */
class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Attempts to sign in a user with email and password
     * @param email User's email
     * @param password User's password
     * @return Flow containing Result with User if successful
     */
    fun login(email: String, password: String): Flow<Result<User>> = flow {
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                val userDoc = firestore.collection("users")
                    .document(firebaseUser.uid)
                    .get()
                    .await()

                val user = User(
                    userId = firebaseUser.uid,
                    displayName = firebaseUser.displayName,
                    authLevel = userDoc.getString("authLevel"),
                    createdRecipes = userDoc.get("createdRecipes") as? MutableList<String>,
                    favoriteRecipe = userDoc.get("favoriteRecipes") as? MutableList<String>
                )
                emit(Result.success(user))
            } ?: emit(Result.failure(Exception("Login failed")))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Checks if a user is currently logged in
     * @return Currently logged in user or null
     */
    fun getCurrentUser(): User? {
        return auth.currentUser?.let { firebaseUser ->
            User(
                userId = firebaseUser.uid,
                displayName = firebaseUser.displayName,
                authLevel = null,
                createdRecipes = null,
                favoriteRecipe = null
            )
        }
    }

    /**
     * Signs out the current user
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Creates a new user account
     * @param email User's email
     * @param password User's password
     * @param username User's display name
     * @return Flow containing Result with created User if successful
     */
    fun createUser(email: String, password: String, username: String): Flow<Result<User>> = flow {
        try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            authResult.user?.let { firebaseUser ->
                // Update display name
                val profileUpdates = userProfileChangeRequest {
                    displayName = username
                }
                firebaseUser.updateProfile(profileUpdates).await()

                // Create user document in Firestore
                val user = User(
                    userId = firebaseUser.uid,
                    displayName = username,
                    authLevel = "User",
                    createdRecipes = mutableListOf(),
                    favoriteRecipe = mutableListOf()
                )

                // Store user data in Firestore
                firestore.collection("users").document(firebaseUser.uid)
                    .set(user)
                    .await()

                emit(Result.success(user))
            } ?: emit(Result.failure(Exception("Failed to create user")))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
} 