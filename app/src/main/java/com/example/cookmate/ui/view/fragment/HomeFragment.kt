package com.example.cookmate.ui.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cookmate.data.model.Ingredient
import com.example.cookmate.data.model.Recipe
import com.example.cookmate.databinding.FragmentHomeBinding
import com.example.cookmate.ui.adapter.RecipeAdapter
import com.example.cookmate.ui.view.activity.RecipeDetailsActivity
import com.google.android.material.chip.Chip

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Lazy initialize the adapter to avoid unnecessary object creation
    private val recipeAdapter by lazy {
        RecipeAdapter { recipe ->
            navigateToRecipeDetails(recipe)
        }
    }

    // Create the fragment view
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Initialize the fragment view
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupChipGroup()
        loadInitialData()
    }

    // Set up the recipes RecyclerView
    private fun setupRecyclerView() {
        binding.recipesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recipeAdapter
        }
    }

    // Set up the chip group
    private fun setupChipGroup() {
        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                recipeAdapter.submitList(getSampleData())
            } else {
                val chip = group.findViewById<Chip>(checkedIds.first())
                val category = chip?.text?.toString()
                filterRecipes(category)
            }
        }
    }

    // Load initial data
    private fun loadInitialData() {
        recipeAdapter.submitList(getSampleData())
    }

    // Filter recipes by category
    private fun filterRecipes(category: String?) {
        val filteredRecipes = getSampleData().filter { recipe ->
            category == null || recipe.categories.contains(category)
        }
        recipeAdapter.submitList(filteredRecipes)
    }

    /**
     * sample data for testing
     * TODO: Remove this when actual data source is implemented
     */
    private fun getSampleData(): List<Recipe> {
        return listOf(
            Recipe(
                0, "Easy", 5f, "Test",
                ingredients = listOf(
                    Ingredient("5", "Bread")
                ), "Cook it up",
                cookingTime = "1 hr",
                prepTime = "1 hr",
                servingSize = "2 people",
                categories = listOf("Breakfast"),
                isDraft = false,
                authorId = "123124asdasdasd",
                recipeDescription = "Food",
                calories = 123f,
                fat = 12f to "grams",
                carbs = 12f to "grams",
                protein = 12f to "grams"
            )
        )
    }

    /**
     * Navigates to the recipe details screen
     * TODO: use Navigation component
     */
    private fun navigateToRecipeDetails(recipe: Recipe) {
        val intent = Intent(requireContext(), RecipeDetailsActivity::class.java).apply {
            putExtra("recipe_name", recipe.title)
            putExtra("recipe_image", recipe.imageRes)
            putExtra("recipe_difficulty", recipe.difficulty)
            putExtra("recipe_time", recipe.prepTime)
            putExtra("recipe_servings", recipe.servingSize)
            putExtra("recipe_rating", recipe.rating)
        }
        startActivity(intent)
    }


    // Destroy the fragment view
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}