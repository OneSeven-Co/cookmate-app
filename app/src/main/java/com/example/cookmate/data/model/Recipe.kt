package com.example.cookmate.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Recipe data class

data class Recipe(
    val imageRes: Int? = null,
    val difficulty: String? = null,
    val rating: Float = 0f,

    val title: String,
    val ingredients: List<Ingredient>,
    val preparationSteps: String,
    val cookingTime: String,
    val prepTime: String,
    val servingSize: String,
    val categories: List<String>,
    val isDraft: Boolean,
    val authorId: String,
    val recipeDescription: String,
    val calories: Float,
    val fat: Pair<Float, String>,
    val carbs: Pair<Float, String>,
    val protein: Pair<Float, String>,
)

@Parcelize
data class Ingredient(
    val amount: String? = null,
    val name: String? = null,
    val substitute: Ingredient? = null,
) : Parcelable

data class AlternativeIngredient(
    val amount: String? = null,
    val name: String? = null,
    val originalIngredient: String? = null
)