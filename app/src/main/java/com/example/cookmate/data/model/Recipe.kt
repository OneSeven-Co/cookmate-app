package com.example.cookmate.data.model

// Recipe data class
data class Recipe(
    val name: String,
    val imageRes: Int,
    val category: String,
    val difficulty: String? = null,
    val time: String? = null,
    val servings: String? = null,
    val rating: Float = 0f,

    val authorId: String = ""
)

data class Ingredient(
    val amount: String? = null,
    val name: String? = null
)

data class AlternativeIngredient(
    val amount: String? = null,
    val name: String? = null,
    val originalIngredient: String? = null
)