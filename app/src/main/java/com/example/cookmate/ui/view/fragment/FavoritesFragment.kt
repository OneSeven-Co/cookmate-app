package com.example.cookmate.ui.view.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cookmate.data.model.Recipe
import com.example.cookmate.data.repository.RecipeRepository
import com.example.cookmate.databinding.FragmentFavoritesBinding
import com.example.cookmate.ui.adapter.RecipeAdapter
import com.example.cookmate.ui.view.activity.RecipeDetailsActivity
import com.example.cookmate.ui.viewmodel.FavoritesViewModel
import com.example.cookmate.ui.viewmodel.FavoritesViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class FavoritesFragment : Fragment() {
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    
    private val recipeAdapter by lazy {
        RecipeAdapter { recipe ->
            navigateToRecipeDetails(recipe)
        }
    }

    private val viewModel: FavoritesViewModel by viewModels {
        FavoritesViewModelFactory(RecipeRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        loadFavorites()
    }

    private fun setupRecyclerView() {
        binding.favoritesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recipeAdapter
        }
    }

    private fun setupObservers() {
        viewModel.favorites.observe(viewLifecycleOwner) { result ->
            result.onSuccess { recipes ->
                Log.d("FavoritesFragment", "Received ${recipes.size} favorites")
                if (recipes.isEmpty()) {
                    binding.emptyStateText.visibility = View.VISIBLE
                    binding.favoritesRecyclerView.visibility = View.GONE
                } else {
                    binding.emptyStateText.visibility = View.GONE
                    binding.favoritesRecyclerView.visibility = View.VISIBLE
                    recipeAdapter.submitList(recipes)
                }
            }.onFailure { exception ->
                Log.e("FavoritesFragment", "Error loading favorites", exception)
                Toast.makeText(context, "Error loading favorites: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadFavorites() {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            viewModel.loadFavorites(user.uid)
        }
    }

    /**
     * Navigates to the recipe details screen with all necessary recipe data
     * @param recipe The recipe to display details for
     */
    private fun navigateToRecipeDetails(recipe: Recipe) {
        val intent = Intent(requireContext(), RecipeDetailsActivity::class.java).apply {
            putExtra("recipe_name", recipe.title)
            putExtra("recipe_description", recipe.recipeDescription)
            putExtra("recipe_image_url", recipe.localImagePath)
            putExtra("recipe_image", recipe.imageRes)
            putExtra("recipe_prep_time", recipe.prepTime)
            putExtra("recipe_time", recipe.cookingTime)
            putExtra("recipe_servings", recipe.servingSize)
            putExtra("recipe_preparation_steps", recipe.preparationSteps)
            putExtra("calories", recipe.calories)
            putExtra("fat", recipe.fat)
            putExtra("carbs", recipe.carbs)
            putExtra("protein", recipe.protein)
            putExtra("ingredients", ArrayList(recipe.ingredients))
            putExtra("from_favorites", true)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 