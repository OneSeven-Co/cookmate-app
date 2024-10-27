package com.example.cookmate.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cookmate.R
import com.example.cookmate.data.model.Recipe
import com.example.cookmate.databinding.FragmentHomeBinding
import com.example.cookmate.ui.adapter.RecipeAdapter
import com.example.cookmate.ui.details.RecipeDetailsActivity
import com.google.android.material.chip.Chip

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
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
            category == null || recipe.category == category
        }
        recipeAdapter.submitList(filteredRecipes)
    }

    // Get sample recipe data
    //TODO: Replace with actual recipe data retrieval logic
    private fun getSampleData(): List<Recipe> {
        return listOf(
            Recipe("Recipe 1", R.drawable.ic_recipe_placeholder, "breakfast", "Easy", "10 min", "2 servings", 4.5f),
            Recipe("Recipe 2", R.drawable.ic_recipe_placeholder, "lunch", "Medium", "30 min", "4 servings", 4.5f),
            Recipe("Recipe 3", R.drawable.ic_recipe_placeholder, "dinner", "Hard", "45 min", "5 servings", 4.5f),
            Recipe("Recipe 4", R.drawable.ic_recipe_placeholder, "breakfast", "Hard", "45 min", "5 servings", 4.5f),
            Recipe("Recipe 5", R.drawable.ic_recipe_placeholder, "lunch", "Hard", "45 min", "5 servings", 4.5f),
            Recipe("Recipe 6", R.drawable.ic_recipe_placeholder, "dinner", "Hard", "45 min", "5 servings", 4.5f)
        )
    }

    // Navigate to recipe details
    private fun navigateToRecipeDetails(recipe: Recipe) {
        val intent = Intent(requireContext(), RecipeDetailsActivity::class.java).apply {
            putExtra("recipe_name", recipe.name)
            putExtra("recipe_image", recipe.imageRes)
            putExtra("recipe_difficulty", recipe.difficulty)
            putExtra("recipe_time", recipe.time)
            putExtra("recipe_servings", recipe.servings)
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