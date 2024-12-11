package com.example.cookmate.ui.view.activity

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cookmate.R
import com.example.cookmate.data.model.Ingredient
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
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_recipe_placeholder)
                    .into(binding.recipeImage)
            } else {
                binding.recipeImage.setImageResource(
                    bundle.getInt("recipe_image", R.drawable.ic_recipe_placeholder)
                )
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
            val ingredients = getSerializable(this, "ingredients", ArrayList::class.java) as ArrayList<Ingredient>
            ingredientsAdapter = IngredientsAdapter(ingredients)
            
            // Preparation steps
            binding.preparationSteps.text = bundle.getString("recipe_preparation_steps", "")
        }
        ingredientsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@RecipeDetailsActivity)
            adapter = ingredientsAdapter
        }

        saveButton.setOnClickListener(){

            if (currentUser == null) {
                Log.e("RecipeDetailsActivity", "User not signed in")
                return@setOnClickListener
            }
            val recipe = mapOf(
                "name" to binding.recipeName.text.toString(),
                "description" to binding.recipeDescription.text.toString(),
                "imageUrl" to intent.extras?.getString("recipe_image_url"),
                "prepTime" to binding.prepTimeValue.text.toString(),
                "cookingTime" to binding.cookingTimeValue.text.toString(),
                "servings" to binding.servingSizeValue.text.toString(),
                "calories" to binding.caloriesText.text.toString(),
                "fat" to binding.fatText.text.toString(),
                "carbs" to binding.carbsText.text.toString(),
                "protein" to binding.proteinText.text.toString(),
                "preparationSteps" to binding.preparationSteps.text.toString(),
                "ingredients" to getSerializable(this, "ingredients", ArrayList::class.java) as ArrayList<Ingredient>
            )
            val userDoc = usersCollection.document(currentUser.uid)
            userDoc.update("favorites", FieldValue.arrayUnion(recipe))
                .addOnSuccessListener {
                    Log.d("RecipeDetailsActivity", "Recipe added to favorites successfully")
                    Toast.makeText(this, "Added to favorites!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("RecipeDetailsActivity", "Failed to add recipe to favorites", e)
                    Toast.makeText(this, "Failed to add to favorites", Toast.LENGTH_SHORT).show()
                }
        }
    }
}