package com.example.cookmate.ui.favorite

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cookmate.R
import com.example.cookmate.databinding.FragmentFavoriteBinding
import com.example.cookmate.data.model.Recipe
import com.example.cookmate.ui.adapter.RecipeAdapter
import com.example.cookmate.ui.details.RecipeDetailsActivity

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    // Lazy initialize the adapter to avoid unnecessary object creation
    private val recipeAdapter by lazy {
        RecipeAdapter { recipe ->
            navigateToRecipeDetails(recipe)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadSampleData()
    }

    private fun setupRecyclerView() {
        binding.favoritesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recipeAdapter
        }
    }

    private fun loadSampleData() {
        val sampleRecipes = listOf(
            Recipe("Recipe 1", R.drawable.ic_recipe_placeholder, "breakfast", "Easy", "10 min", "2 servings", 4.5f),
            Recipe("Recipe 2", R.drawable.ic_recipe_placeholder, "lunch", "Medium", "30 min", "4 servings", 4.5f),
            Recipe("Recipe 3", R.drawable.ic_recipe_placeholder, "dinner", "Hard", "45 min", "5 servings", 4.5f),
            Recipe("Recipe 4", R.drawable.ic_recipe_placeholder, "breakfast", "Hard", "45 min", "5 servings", 4.5f),
            Recipe("Recipe 5", R.drawable.ic_recipe_placeholder, "lunch", "Hard", "45 min", "5 servings", 4.5f),
            Recipe("Recipe 6", R.drawable.ic_recipe_placeholder, "dinner", "Hard", "45 min", "5 servings", 4.5f)
        )
        recipeAdapter.submitList(sampleRecipes)
    }

    /**
     * Navigates to the recipe details screen
     * TODO: use Navigation component
     */
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
