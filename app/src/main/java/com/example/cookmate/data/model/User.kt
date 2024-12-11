package com.example.cookmate.data.model

/**
 * Data class representing a user in the application
 * @property userId Unique identifier for the user
 * @property displayName User's display name
 * @property authLevel User's authorization level (e.g., "User", "Admin")
 * @property createdRecipes List of recipe IDs created by the user
 * @property favoriteRecipe List of recipe IDs favorited by the user
 */
data class User(
    val userId: String,
    val displayName: String?,
    val authLevel: String?,
    val createdRecipes: MutableList<String>?,
    val favoriteRecipe: MutableList<String>?
)