package com.example.cookmate.ui.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cookmate.R
import com.example.cookmate.data.model.Ingredient
import com.example.cookmate.data.model.Recipe
import com.example.cookmate.data.repository.RecipeRepository
import com.example.cookmate.databinding.FragmentHomeBinding
import com.example.cookmate.ui.adapter.RecipeAdapter
import com.example.cookmate.ui.view.activity.RecipeDetailsActivity
import com.example.cookmate.ui.viewmodel.HomeViewModel
import com.example.cookmate.ui.viewmodel.HomeViewModelFactory
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

    // Add ViewModel
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(RecipeRepository())
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
        setupObservers()
        viewModel.loadPublishedRecipes()
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
                viewModel.loadPublishedRecipes()
            } else {
                val chip = group.findViewById<Chip>(checkedIds.first())
                chip?.text?.toString()?.let { category ->
                    viewModel.loadRecipesByCategory(category)
                }
            }
        }
    }

    // Set up observers
    private fun setupObservers() {
        viewModel.recipes.observe(viewLifecycleOwner) { result ->
            result.onSuccess { recipes ->
                recipeAdapter.submitList(recipes)
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
            putExtra("recipe_description", recipe.recipeDescription)
            putExtra("recipe_image_url", recipe.imageUrl)
            putExtra("recipe_image", recipe.imageRes)
            putExtra("recipe_prep_time", recipe.prepTime)
            putExtra("recipe_time", recipe.cookingTime)
            putExtra("recipe_servings", recipe.servingSize)
            putExtra("recipe_preparation_steps", recipe.preparationSteps)
            putExtra("calories", recipe.calories)
            putExtra("fat", recipe.fat)
            putExtra("carbs", recipe.carbs)
            putExtra("protein", recipe.protein)
            putParcelableArrayListExtra("ingredients", ArrayList(recipe.ingredients))
        }
        startActivity(intent)
    }


    // Destroy the fragment view
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}