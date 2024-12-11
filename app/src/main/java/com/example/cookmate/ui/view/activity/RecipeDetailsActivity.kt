package com.example.cookmate.ui.view.activity

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import java.io.Serializable

class RecipeDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeDetailsBinding
    private lateinit var ingredientsRecyclerView: RecyclerView
    private lateinit var ingredientsAdapter: IngredientsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRecipeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ingredientsRecyclerView = findViewById(R.id.ingredientsRecyclerView)

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

            binding.caloriesText.text = "Calories: ${calories?.first ?: 0} ${calories?.second ?: "kcal"}"
            binding.fatText.text = "Fat: ${fat?.first ?: 0} ${fat?.second ?: "g"}"
            binding.carbsText.text = "Carbohydrates: ${carbs?.first ?: 0} ${carbs?.second ?: "g"}"
            binding.proteinText.text = "Protein: ${protein?.first ?: 0} ${protein?.second ?: "g"}"

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
    }
}