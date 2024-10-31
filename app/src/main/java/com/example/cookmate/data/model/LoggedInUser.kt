package com.example.cookmate.data.model

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    val userId: String,
    val displayName: String?,

    val authLevel: String?,
    val createdRecipes: MutableList<String>?,
    val favoriteRecipe: MutableList<String>?
)