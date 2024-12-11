package com.example.cookmate.data.repository

import com.example.cookmate.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/**
 * Repository class that handles user-related operations
 */
class UserRepository : BaseRepository<User> {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    /**
     * Gets all users from Firestore
     * @return Flow containing Result with list of users
     */
    override fun getAll(): Flow<Result<List<User>>> = getAllUsers()

    /**
     * Updates a user's field in Firestore
     * @param id The document ID
     * @param field The field to update
     * @param value The new value
     * @return Flow containing Result indicating success or failure
     */
    override fun updateField(id: String, field: String, value: Any): Flow<Result<Unit>> = 
        updateUserField(id, field, value)

    /**
     * Deletes a user from Firestore
     * @param id The document ID to delete
     * @return Flow containing Result indicating success or failure
     */
    override fun deleteDocument(id: String): Flow<Result<Unit>> = flow {
        try {
            usersCollection.document(id)
                .delete()
                .await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Gets all users from Firestore
     * @return Flow containing Result with list of users
     */
    fun getAllUsers(): Flow<Result<List<User>>> = flow {
        try {
            val snapshot = usersCollection.get().await()
            val users = snapshot.documents.mapNotNull { doc ->
                User(
                    userId = doc.id,
                    displayName = doc.getString("username"),
                    authLevel = doc.getString("authLevel"),
                    createdRecipes = doc.get("createdRecipes") as? MutableList<String>,
                    favoriteRecipe = doc.get("favoriteRecipes") as? MutableList<String>
                )
            }
            emit(Result.success(users))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Updates a user's field in Firestore
     * @param userId The ID of the user to update
     * @param field The field to update
     * @param value The new value
     * @return Flow containing Result indicating success or failure
     */
    fun updateUserField(userId: String, field: String, value: Any): Flow<Result<Unit>> = flow {
        try {
            usersCollection.document(userId)
                .update(field, value)
                .await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Checks if a username already exists in the database
     * @param username The username to check
     * @return Flow containing Result with boolean indicating if username exists
     */
    fun checkUsernameExists(username: String): Flow<Result<Boolean>> = flow {
        try {
            val snapshot = usersCollection
                .whereEqualTo("username", username)
                .get()
                .await()
            emit(Result.success(!snapshot.isEmpty))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
} 