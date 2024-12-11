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
    val calories: Pair<Float, String>,
    val fat: Pair<Float, String>,
    val carbs: Pair<Float, String>,
    val protein: Pair<Float, String>,
)

/**
 * Represents an ingredient in a recipe
 * @property amount The quantity of the ingredient
 * @property unit The unit of measurement (can be null for items like "1 whole apple")
 * @property name The name of the ingredient
 * @property substitutes Optional list of possible substitute ingredients
 */
@Parcelize
data class Ingredient(
    val amount: Float = 0f,
    val unit: String? = "",
    val name: String = "",
    val substitutes: List<String>? = emptyList()
) : Parcelable