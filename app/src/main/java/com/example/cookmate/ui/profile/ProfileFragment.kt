package com.example.cookmate.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cookmate.R
import com.example.cookmate.databinding.FragmentProfileBinding
import com.example.cookmate.ui.adapter.RecipeAdapter
import com.example.cookmate.data.model.Recipe
import com.example.cookmate.ui.details.RecipeDetailsActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    // Lazy initialize the adapter to avoid unnecessary object creation
    private val userRecipesAdapter by lazy {
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
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        // Get FirebaseAuth instance
        val firebaseAuth = FirebaseAuth.getInstance()

        // Get the current user
        val user = firebaseAuth.currentUser

        // Get the display name
        if (user != null) {
            val displayName = user.displayName
            binding.profileName.text = displayName ?: "No name available"
        } else {
            binding.profileName.text = "No user signed in"
        }
        return binding.root
    }

    // Load sample data for user's recipes
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadInitialData()
    }

    // Set up the recipes RecyclerView
    private fun setupRecyclerView() {
        binding.recipesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userRecipesAdapter
        }
    }

    private fun loadInitialData() {
        // Load sample data for user's recipes
        userRecipesAdapter.submitList(getSampleUserRecipes())
    }

    /**
     * sample data for testing
     * TODO: Remove this when actual data source is implemented
     */
    private fun getSampleUserRecipes(): List<Recipe> {
        // TODO: Replace with actual user recipes data retrieval logic
        return listOf(
            Recipe(
                name = "My Recipe 1",
                imageRes = R.drawable.ic_recipe_placeholder,
                category = "breakfast",
                difficulty = "Easy",
                time = "15 min",
                servings = "2 servings",
                rating = 4.5f
            ),
            Recipe(
                name = "My Recipe 2",
                imageRes = R.drawable.ic_recipe_placeholder,
                category = "lunch",
                difficulty = "Medium",
                time = "25 min",
                servings = "4 servings",
                rating = 4.5f
            ),
            Recipe(
                name = "My Recipe 3",
                imageRes = R.drawable.ic_recipe_placeholder,
                category = "dinner",
                difficulty = "Hard",
                time = "45 min",
                servings = "6 servings",
                rating = 4.5f
            ),
            Recipe(
                name = "My Recipe 4",
                imageRes = R.drawable.ic_recipe_placeholder,
                category = "dinner",
                difficulty = "Hard",
                time = "45 min",
                servings = "6 servings",
                rating = 4.5f
            ),
            Recipe(
                name = "My Recipe 5",
                imageRes = R.drawable.ic_recipe_placeholder,
                category = "dinner",
                difficulty = "Hard",
                time = "45 min",
                servings = "6 servings",
                rating = 4.5f
            ),
            Recipe(
                name = "My Recipe 6",
                imageRes = R.drawable.ic_recipe_placeholder,
                category = "dinner",
                difficulty = "Hard",
                time = "45 min",
                servings = "6 servings",
                rating = 4.5f
            )
        )
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