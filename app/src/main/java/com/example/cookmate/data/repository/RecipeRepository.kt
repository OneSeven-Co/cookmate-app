package com.example.cookmate.data.repository

import com.example.cookmate.data.model.Recipe
import com.example.cookmate.data.model.Ingredient
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import android.util.Log

/**
 * Repository class that handles recipe-related operations
 */
class RecipeRepository : BaseRepository<Recipe> {
    private val firestore = FirebaseFirestore.getInstance()
    private val recipesCollection = firestore.collection("recipes")
    private val ingredientsCollection = firestore.collection("ingredients")

    /**
     * Stores a new recipe in Firestore
     * @param recipe The recipe to store
     * @return Flow containing Result with the recipe ID if successful
     */
    fun storeRecipe(recipe: Recipe): Flow<Result<String>> = flow {
        try {
            val recipeData = hashMapOf(
                "title" to recipe.title,
                "ingredients" to recipe.ingredients.map { ingredient ->
                    mapOf(
                        "amount" to ingredient.amount,
                        "unit" to ingredient.unit,
                        "name" to ingredient.name,
                        "substitutes" to ingredient.substitutes
                    )
                },
                "preparationSteps" to recipe.preparationSteps,
                "cookingTime" to recipe.cookingTime,
                "prepTime" to recipe.prepTime,
                "servingSize" to recipe.servingSize,
                "categories" to recipe.categories,
                "difficulty" to recipe.difficulty,
                "isDraft" to recipe.isDraft,
                "authorId" to recipe.authorId,
                "imageRes" to recipe.imageRes,
                "localImagePath" to recipe.localImagePath,
                "rating" to recipe.rating,
                "recipeDescription" to recipe.recipeDescription,
                "calories" to recipe.calories,
                "fat" to recipe.fat,
                "carbs" to recipe.carbs,
                "protein" to recipe.protein
            )

            val documentRef = recipesCollection.add(recipeData).await()
            emit(Result.success(documentRef.id))
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error storing recipe", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Updates a field in a recipe document
     * @param id The recipe ID
     * @param field The field to update
     * @param value The new value
     * @return Flow containing Result indicating success or failure
     */
    override fun updateField(id: String, field: String, value: Any): Flow<Result<Unit>> = flow {
        try {
            recipesCollection.document(id)
                .update(field, value)
                .await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Deletes a recipe document
     * @param id The recipe ID to delete
     * @return Flow containing Result indicating success or failure
     */
    override fun deleteDocument(id: String): Flow<Result<Unit>> = flow {
        try {
            recipesCollection.document(id)
                .delete()
                .await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Gets all recipes from Firestore
     * @return Flow containing Result with list of recipes
     */
    override fun getAll(): Flow<Result<List<Recipe>>> = flow {
        try {
            val snapshot = recipesCollection.get().await()
            val recipes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Recipe::class.java)
            }
            emit(Result.success(recipes))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Gets all recipes for a specific user
     * @param userId The ID of the user
     * @return Flow containing Result with list of recipes
     */
    fun getRecipesForUser(userId: String): Flow<Result<List<Recipe>>> = flow {
        try {
            val snapshot = recipesCollection
                .whereEqualTo("authorId", userId)
                .get()
                .await()
            
            val recipes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Recipe::class.java)
            }
            emit(Result.success(recipes))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Gets all available ingredients
     * @return Flow containing Result with list of ingredients
     */
    fun getAllIngredients(): Flow<Result<List<Ingredient>>> = flow {
        try {
            val snapshot = ingredientsCollection.get().await()
            val ingredients = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Ingredient::class.java)
            }
            emit(Result.success(ingredients))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Gets all published recipes (non-draft) from Firestore
     * @return Flow containing Result with list of published recipes
     */
    fun getAllPublishedRecipes(): Flow<Result<List<Recipe>>> = flow {
        try {
            val snapshot = recipesCollection
                .whereEqualTo("isDraft", false)
                .get()
                .await()
            
            val recipes = snapshot.documents.mapNotNull { doc ->
                try {
                    Recipe(
                        title = doc.getString("title") ?: "",
                        ingredients = (doc.get("ingredients") as? List<*>)?.mapNotNull { ingredient ->
                            (ingredient as? Map<*, *>)?.let {
                                Ingredient(
                                    amount = (it["amount"] as? Number)?.toFloat() ?: 0f,
                                    unit = it["unit"] as? String,
                                    name = it["name"] as? String ?: "",
                                    substitutes = (it["substitutes"] as? List<*>)?.mapNotNull { sub -> sub as? String }
                                )
                            }
                        } ?: emptyList(),
                        preparationSteps = doc.getString("preparationSteps") ?: "",
                        cookingTime = doc.getString("cookingTime") ?: "",
                        prepTime = doc.getString("prepTime") ?: "",
                        servingSize = doc.getString("servingSize") ?: "",
                        categories = (doc.get("categories") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        isDraft = doc.getBoolean("isDraft") ?: false,
                        authorId = doc.getString("authorId") ?: "",
                        imageRes = doc.getLong("imageRes")?.toInt(),
                        localImagePath = doc.getString("localImagePath"),
                        difficulty = doc.getString("difficulty"),
                        rating = (doc.getDouble("rating")?.toFloat() ?: 0f),
                        recipeDescription = doc.getString("recipeDescription") ?: "",
                        calories = Pair(
                            (doc.getDouble("calories.first")?.toFloat() ?: 0f),
                            doc.getString("calories.second") ?: "kcal"
                        ),
                        fat = Pair(
                            (doc.getDouble("fat.first")?.toFloat() ?: 0f),
                            doc.getString("fat.second") ?: "g"
                        ),
                        carbs = Pair(
                            (doc.getDouble("carbs.first")?.toFloat() ?: 0f),
                            doc.getString("carbs.second") ?: "g"
                        ),
                        protein = Pair(
                            (doc.getDouble("protein.first")?.toFloat() ?: 0f),
                            doc.getString("protein.second") ?: "g"
                        )
                    )
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error mapping document: ${doc.id}", e)
                    null
                }
            }
            emit(Result.success(recipes))
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error getting recipes", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Gets all recipes for a specific category
     * @param category The category to filter by
     * @return Flow containing Result with list of recipes in the category
     */
    fun getRecipesByCategory(category: String): Flow<Result<List<Recipe>>> = flow {
        try {
            val snapshot = recipesCollection
                .whereEqualTo("isDraft", false)
                .whereArrayContains("categories", category)
                .get()
                .await()
            
            val recipes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Recipe::class.java)
            }
            emit(Result.success(recipes))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Gets all recipes created by a specific user
     * @param userId The ID of the user
     * @return Flow containing Result with list of user's recipes
     */
    fun getUserRecipes(userId: String): Flow<Result<List<Recipe>>> = flow {
        try {
            val snapshot = recipesCollection
                .whereEqualTo("authorId", userId)
                .get()
                .await()
            
            val recipes = snapshot.documents.mapNotNull { doc ->
                try {
                    Recipe(
                        title = doc.getString("title") ?: "",
                        ingredients = (doc.get("ingredients") as? List<*>)?.mapNotNull { ingredient ->
                            (ingredient as? Map<*, *>)?.let { map ->
                                Ingredient(
                                    amount = (map["amount"] as? Number)?.toFloat() ?: 0f,
                                    unit = map["unit"] as? String,
                                    name = map["name"] as? String ?: "",
                                    substitutes = (map["substitutes"] as? List<*>)?.mapNotNull { sub -> sub as? String }
                                )
                            }
                        } ?: emptyList(),
                        preparationSteps = doc.getString("preparationSteps") ?: "",
                        cookingTime = doc.getString("cookingTime") ?: "",
                        prepTime = doc.getString("prepTime") ?: "",
                        servingSize = doc.getString("servingSize") ?: "",
                        categories = (doc.get("categories") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        isDraft = doc.getBoolean("isDraft") ?: false,
                        authorId = doc.getString("authorId") ?: "",
                        imageRes = doc.getLong("imageRes")?.toInt(),
                        localImagePath = doc.getString("localImagePath"),
                        difficulty = doc.getString("difficulty") ?: "",
                        rating = doc.getDouble("rating")?.toFloat() ?: 0f,
                        recipeDescription = doc.getString("recipeDescription") ?: "",
                        calories = Pair(
                            (doc.getDouble("calories.first")?.toFloat() ?: 0f),
                            doc.getString("calories.second") ?: "kcal"
                        ),
                        fat = Pair(
                            (doc.getDouble("fat.first")?.toFloat() ?: 0f),
                            doc.getString("fat.second") ?: "g"
                        ),
                        carbs = Pair(
                            (doc.getDouble("carbs.first")?.toFloat() ?: 0f),
                            doc.getString("carbs.second") ?: "g"
                        ),
                        protein = Pair(
                            (doc.getDouble("protein.first")?.toFloat() ?: 0f),
                            doc.getString("protein.second") ?: "g"
                        )
                    )
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error mapping document: ${doc.id}", e)
                    null
                }
            }
            emit(Result.success(recipes))
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error getting user recipes", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Gets all favorite recipes for a specific user
     * @param userId The ID of the user whose favorites to retrieve
     * @return Flow containing Result with list of favorite recipes
     */
    fun getFavoriteRecipes(userId: String): Flow<Result<List<Recipe>>> = flow {
        try {
            val snapshot = firestore.collection("favorites")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val recipes = snapshot.documents.mapNotNull { doc ->
                try {
                    val recipeMap = doc.get("recipe") as? Map<*, *>
                    recipeMap?.let {
                        Recipe(
                            title = it["title"] as? String ?: "",
                            ingredients = (it["ingredients"] as? List<*>)?.mapNotNull { ingredient ->
                                (ingredient as? Map<*, *>)?.let { map ->
                                    Ingredient(
                                        amount = (map["amount"] as? Number)?.toFloat() ?: 0f,
                                        unit = map["unit"] as? String,
                                        name = map["name"] as? String ?: "",
                                        substitutes = (map["substitutes"] as? List<*>)?.mapNotNull { sub -> sub as? String }
                                    )
                                }
                            } ?: emptyList(),
                            preparationSteps = it["preparationSteps"] as? String ?: "",
                            cookingTime = it["cookingTime"] as? String ?: "",
                            prepTime = it["prepTime"] as? String ?: "",
                            servingSize = it["servingSize"] as? String ?: "",
                            categories = (it["categories"] as? List<*>)?.mapNotNull { cat -> cat as? String } ?: emptyList(),
                            isDraft = it["isDraft"] as? Boolean ?: false,
                            authorId = it["authorId"] as? String ?: "",
                            imageRes = (it["imageRes"] as? Number)?.toInt(),
                            localImagePath = it["localImagePath"] as? String,
                            difficulty = it["difficulty"] as? String ?: "",
                            rating = (it["rating"] as? Number)?.toFloat() ?: 0f,
                            recipeDescription = it["recipeDescription"] as? String ?: "",
                            calories = Pair(
                                (it["calories.first"] as? Number)?.toFloat() ?: 0f,
                                it["calories.second"] as? String ?: "kcal"
                            ),
                            fat = Pair(
                                (it["fat.first"] as? Number)?.toFloat() ?: 0f,
                                it["fat.second"] as? String ?: "g"
                            ),
                            carbs = Pair(
                                (it["carbs.first"] as? Number)?.toFloat() ?: 0f,
                                it["carbs.second"] as? String ?: "g"
                            ),
                            protein = Pair(
                                (it["protein.first"] as? Number)?.toFloat() ?: 0f,
                                it["protein.second"] as? String ?: "g"
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error mapping favorite document: ${doc.id}", e)
                    null
                }
            }
            emit(Result.success(recipes))
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error getting favorite recipes", e)
            emit(Result.failure(e))
        }
    }
} 