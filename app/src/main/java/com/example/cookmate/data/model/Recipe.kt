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

@Parcelize
data class Ingredient(
    val amount: Float = 0f, //Initialized to 0, must be > 0 when submitting recipes in creation
    val unit: String? = null, //Nullable just to prevent awkward units (what's the unit of measurement for "1 melon"?)
    val name: String,
    val substitutes: List<String>? = null, //Will populate with Ingredient names in Firestore in a relational manner and load clientside with a query
) : Parcelable