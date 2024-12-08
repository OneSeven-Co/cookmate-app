package com.example.cookmate.ui.view.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cookmate.R
import com.example.cookmate.data.model.Ingredient
import com.example.cookmate.databinding.ActivityRecipeDetailsBinding
import com.example.cookmate.ui.adapter.IngredientsAdapter

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

        // Get recipe details from intent
        intent.extras?.let { bundle ->
            binding.recipeName.text = bundle.getString("recipe_name", "")
            binding.recipeImage.setImageResource(bundle.getInt("recipe_image", R.drawable.ic_recipe_placeholder))

            // Set cooking info
            binding.prepTimeValue.text = bundle.getString("recipe_time", "N/A")
            binding.servingSizeValue.text = bundle.getString("recipe_servings", "N/A")

            val ingredients = bundle.getParcelableArrayList<Ingredient>("ingredients") ?: emptyList()
            Log.d("ingredients size", "${ingredients.size}")
            ingredientsAdapter = IngredientsAdapter(ingredients)
        }
        ingredientsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@RecipeDetailsActivity)
            adapter = ingredientsAdapter
        }
    }
}