package com.example.cookmate.ui.view.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cookmate.databinding.FragmentProfileBinding
import com.example.cookmate.ui.adapter.RecipeAdapter
import com.example.cookmate.data.model.Recipe
import com.example.cookmate.ui.view.activity.RecipeDetailsActivity
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.cookmate.data.repository.RecipeRepository
import com.example.cookmate.ui.viewmodel.ProfileViewModel
import com.example.cookmate.ui.viewmodel.ProfileViewModelFactory

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

    // Add ViewModel
    private val viewModel: ProfileViewModel by viewModels { 
        ProfileViewModelFactory(RecipeRepository()) 
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
        setupObservers()
        
        // Load current user's recipes
        FirebaseAuth.getInstance().currentUser?.let { user ->
            viewModel.loadUserRecipes(user.uid)
        }
    }

    // Set up the recipes RecyclerView
    private fun setupRecyclerView() {
        binding.recipesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userRecipesAdapter
        }
    }

    private fun setupObservers() {
        viewModel.recipes.observe(viewLifecycleOwner) { result ->
            result.onSuccess { recipes ->
                userRecipesAdapter.submitList(recipes)
            }.onFailure { exception ->
                Toast.makeText(
                    context,
                    "Error loading recipes: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
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