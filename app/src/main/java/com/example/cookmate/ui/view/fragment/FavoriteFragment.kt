package com.example.cookmate.ui.view.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cookmate.data.model.Ingredient
import com.example.cookmate.databinding.FragmentFavoriteBinding
import com.example.cookmate.data.model.Recipe
import com.example.cookmate.ui.adapter.RecipeAdapter
import com.example.cookmate.ui.view.activity.RecipeDetailsActivity

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
            Recipe(
                0, "Easy", "Hard", 2.0f,
                ingredients = listOf(
                    Ingredient(5f, "Slices", "Bread")
                ), title = "Cook it up",
                cookingTime = "1 hr",
                prepTime = "1 hr",
                servingSize = "2 people",
                categories = listOf("Breakfast"),
                isDraft = false,
                authorId = "123124asdasdasd",
                recipeDescription = "Food",
                calories = 123f to "kcal",
                fat = 12f to "Grams",
                carbs = 12f to "Grams",
                protein = 12f to "Grams",
                preparationSteps = "None"
            )
        )
        recipeAdapter.submitList(sampleRecipes)
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
            putExtra("calories", recipe.calories)
            putExtra("fat", recipe.fat)
            putExtra("carbs", recipe.carbs)
            putExtra("protein", recipe.protein)
            putParcelableArrayListExtra("ingredients", ArrayList(recipe.ingredients))
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
