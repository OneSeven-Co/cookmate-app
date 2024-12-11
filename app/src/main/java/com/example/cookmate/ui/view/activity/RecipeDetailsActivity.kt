package com.example.cookmate.ui.view.activity

import android.app.Activity
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat.getSerializable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cookmate.R
import com.example.cookmate.data.model.Ingredient
import com.example.cookmate.data.model.Recipe
import com.example.cookmate.databinding.ActivityRecipeDetailsBinding
import com.example.cookmate.ui.adapter.IngredientsAdapter
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.io.Serializable

class RecipeDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeDetailsBinding
    private lateinit var ingredientsRecyclerView: RecyclerView
    private lateinit var ingredientsAdapter: IngredientsAdapter
    private lateinit var saveButton: MaterialButton
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    private fun saveToFavorites() {
        if (currentUser == null) {
            Log.e("RecipeDetailsActivity", "User not signed in")
            Toast.makeText(this, "Please sign in to save favorites", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if recipe already exists in favorites
        firestore.collection("favorites")
            .whereEqualTo("userId", currentUser.uid)
            .whereEqualTo("recipe.title", binding.recipeName.text.toString())
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    Toast.makeText(this, "Recipe already in favorites!", Toast.LENGTH_SHORT).show()
                    saveButton.isEnabled = false
                    saveButton.text = "Saved to Favorites"
                    return@addOnSuccessListener
                }

                // If not a duplicate, proceed with saving
                val recipe = Recipe(
                    title = binding.recipeName.text.toString(),
                    ingredients = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getSerializableExtra("ingredients", ArrayList::class.java) as ArrayList<Ingredient>
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getSerializableExtra("ingredients") as ArrayList<Ingredient>
                    },
                    preparationSteps = binding.preparationSteps.text.toString(),
                    cookingTime = binding.cookingTimeValue.text.toString(),
                    prepTime = binding.prepTimeValue.text.toString(),
                    servingSize = binding.servingSizeValue.text.toString(),
                    categories = listOf(),
                    isDraft = false,
                    authorId = currentUser.uid,
                    imageRes = null,
                    localImagePath = intent.extras?.getString("recipe_image_url"),
                    difficulty = "",
                    rating = 5f,
                    recipeDescription = binding.recipeDescription.text.toString(),
                    calories = binding.caloriesText.text.toString().split(":")[1].trim().split(" ").let {
                        it[0].toFloat() to it[1]
                    },
                    fat = binding.fatText.text.toString().split(":")[1].trim().split(" ").let {
                        it[0].toFloat() to it[1]
                    },
                    carbs = binding.carbsText.text.toString().split(":")[1].trim().split(" ").let {
                        it[0].toFloat() to it[1]
                    },
                    protein = binding.proteinText.text.toString().split(":")[1].trim().split(" ").let {
                        it[0].toFloat() to it[1]
                    }
                )

                // Add to Firestore favorites collection
                val favoriteRecipe = hashMapOf(
                    "userId" to currentUser.uid,
                    "recipe" to hashMapOf(
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
                        "isDraft" to recipe.isDraft,
                        "authorId" to recipe.authorId,
                        "imageRes" to recipe.imageRes,
                        "localImagePath" to recipe.localImagePath,
                        "difficulty" to recipe.difficulty,
                        "rating" to recipe.rating,
                        "recipeDescription" to recipe.recipeDescription,
                        "calories" to recipe.calories,
                        "fat" to recipe.fat,
                        "carbs" to recipe.carbs,
                        "protein" to recipe.protein
                    ),
                    "timestamp" to FieldValue.serverTimestamp()
                )

                firestore.collection("favorites")
                    .add(favoriteRecipe)
                    .addOnSuccessListener {
                        Log.d("RecipeDetailsActivity", "Recipe added to favorites successfully")
                        Toast.makeText(this, "Added to favorites!", Toast.LENGTH_SHORT).show()
                        saveButton.isEnabled = false
                        saveButton.text = "Saved to Favorites"
                    }
                    .addOnFailureListener { e ->
                        Log.e("RecipeDetailsActivity", "Failed to add recipe to favorites", e)
                        Toast.makeText(this, "Failed to add to favorites", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("RecipeDetailsActivity", "Error checking for duplicate favorites", e)
                Toast.makeText(this, "Error checking favorites", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Deletes a recipe from the user's favorites
     * Handles user authentication check and provides appropriate feedback
     */
    private fun deleteFavorite() {
        if (currentUser == null) {
            Log.e("RecipeDetailsActivity", "User not signed in")
            Toast.makeText(this, "Please sign in to manage favorites", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("favorites")
            .whereEqualTo("userId", currentUser.uid)
            .whereEqualTo("recipe.title", binding.recipeName.text.toString())
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "Recipe not found in favorites", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val document = documents.documents[0]
                firestore.collection("favorites")
                    .document(document.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.e("RecipeDetailsActivity", "Error deleting favorite", e)
                        Toast.makeText(this, "Failed to remove from favorites", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("RecipeDetailsActivity", "Error finding favorite to delete", e)
                Toast.makeText(this, "Error removing from favorites", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRecipeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ingredientsRecyclerView = findViewById(R.id.ingredientsRecyclerView)
        saveButton = binding.saveToFavorites

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up toolbar navigation
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Helper function to get serializable data from intent
        // This is needed because getSerializableExtra is not available in Android 12
        fun <T : Serializable?> getSerializable(activity: Activity, name: String, clazz: Class<T>): T {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                activity.intent.getSerializableExtra(name, clazz)!!
            else
                activity.intent.getSerializableExtra(name) as T
        }

        // Get recipe details from intent
        intent.extras?.let { bundle ->
            // Recipe basic info
            binding.recipeName.text = bundle.getString("recipe_name", "")
            binding.recipeDescription.text = bundle.getString("recipe_description", "")

            // Image handling
            val imageUrl = bundle.getString("recipe_image_url")
            if (imageUrl != null) {
                Glide.with(this)
                    .load(Uri.parse(imageUrl))
                    .placeholder(R.drawable.ic_recipe_placeholder)
                    .error(R.drawable.ic_recipe_placeholder)
                    .into(binding.recipeImage)
            } else {
                val imageRes = bundle.getInt("recipe_image", R.drawable.ic_recipe_placeholder)
                binding.recipeImage.setImageResource(imageRes ?: R.drawable.ic_recipe_placeholder)
            }

            // Cooking info
            binding.prepTimeValue.text = bundle.getString("recipe_prep_time", "N/A")
            binding.cookingTimeValue.text = bundle.getString("recipe_time", "N/A")
            binding.servingSizeValue.text = bundle.getString("recipe_servings", "N/A")

            // Nutrition info
            val calories = bundle.getSerializable("calories") as? Pair<Float, String>
            val fat = bundle.getSerializable("fat") as? Pair<Float, String>
            val carbs = bundle.getSerializable("carbs") as? Pair<Float, String>
            val protein = bundle.getSerializable("protein") as? Pair<Float, String>

            "Calories: ${calories?.first ?: 0} ${calories?.second ?: "kcal"}".also { binding.caloriesText.text = it }
            "Fat: ${fat?.first ?: 0} ${fat?.second ?: "g"}".also { binding.fatText.text = it }
            "Carbohydrates: ${carbs?.first ?: 0} ${carbs?.second ?: "g"}".also { binding.carbsText.text = it }
            "Protein: ${protein?.first ?: 0} ${protein?.second ?: "g"}".also { binding.proteinText.text = it }

            // Ingredients
            val ingredients = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra("ingredients", ArrayList::class.java) as ArrayList<Ingredient>
            } else {
                @Suppress("DEPRECATION")
                intent.getSerializableExtra("ingredients") as ArrayList<Ingredient>
            }
            ingredientsAdapter = IngredientsAdapter(ingredients)
            
            // Preparation steps
            binding.preparationSteps.text = bundle.getString("recipe_preparation_steps", "")
        }
        ingredientsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@RecipeDetailsActivity)
            adapter = ingredientsAdapter
        }

        // Update button based on where we came from
        if (intent.getBooleanExtra("from_favorites", false)) {
            saveButton.text = "Delete Favorite"
            saveButton.setOnClickListener {
                deleteFavorite()
            }
        } else {
            saveButton.setOnClickListener {
                saveToFavorites()
            }
        }
    }
}