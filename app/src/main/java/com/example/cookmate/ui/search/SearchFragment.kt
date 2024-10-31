package com.example.cookmate.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cookmate.R
import com.example.cookmate.data.model.Recipe
import com.example.cookmate.databinding.FragmentSearchBinding
import com.example.cookmate.ui.adapter.RecipeAdapter
import com.example.cookmate.ui.details.RecipeDetailsActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * SearchFragment handles the search functionality of the app, including:
 * - Real-time search filtering
 * - Category and difficulty filtering
 * - Displaying search results in a RecyclerView
 */
class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    // Store all recipes to avoid making repeated network calls
    // TODO: Replace with proper data repository pattern
    private val allRecipes = mutableListOf<Recipe>()

    private var selectedCategories = mutableSetOf<String>()
    private var selectedDifficulty: String? = null

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
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchBar()
        setupFilterIcon()
        loadInitialData()
    }

    /**
     * Sets up the RecyclerView with a LinearLayoutManager and the recipe adapter
     */
    private fun setupRecyclerView() {
        binding.recipesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recipeAdapter
        }
    }

    /**
     * search bar setup
     * TODO: Add loading state while searching
     */
    private fun setupSearchBar() {
        // Handle search input changes
        binding.searchBar.addTextChangedListener { text ->
            filterRecipes(text?.toString() ?: "")
        }

        // Handle keyboard search action
        binding.searchBar.setOnEditorActionListener { _, _, _ ->
            filterRecipes(binding.searchBar.text?.toString() ?: "")
            true
        }
    }

    /**
     * Sets up the filter icon click listener
     */
    private fun setupFilterIcon() {
        binding.searchBarLayout.setEndIconOnClickListener {
            showFilterDialog()
        }
    }

    /**
     * Shows the filter dialog with categories and difficulty options
     * TODO: Load categories from backend/database
     * TODO: Persist filter selections
     */
    private fun showFilterDialog() {
        val categories = arrayOf("Breakfast", "Lunch", "Dinner", "Desserts", "Appetizers", "Beverages")

        val dialogView = createFilterDialogView(categories)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filter Options")
            .setView(dialogView)
            .setPositiveButton("Apply") { dialog, _ ->
                applyFilters()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // Reset to previous selections
                dialog.dismiss()
            }
            .setNeutralButton("Clear Filters") { _, _ ->
                clearFilters()
            }
            .show()
    }

    /**
     * Creates the custom view for the filter dialog
     * TODO: Add additional filter options
     */
    private fun createFilterDialogView(categories: Array<String>): View {
        val dialogView = layoutInflater.inflate(R.layout.dialog_filter, null)

        // Setup category chips
        val chipGroup = dialogView.findViewById<ChipGroup>(R.id.categoryChipGroup)
        categories.forEach { category ->
            val chip = createCategoryChip(category)
            chipGroup.addView(chip)
        }

        // Setup difficulty radio group
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.difficultyRadioGroup)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedDifficulty = when (checkedId) {
                R.id.easyRadio -> "Easy"
                R.id.mediumRadio -> "Medium"
                R.id.hardRadio -> "Hard"
                else -> null
            }
        }

        // Set previous selections if any
        selectedDifficulty?.let { difficulty ->
            val radioButtonId = when (difficulty) {
                "Easy" -> R.id.easyRadio
                "Medium" -> R.id.mediumRadio
                "Hard" -> R.id.hardRadio
                else -> null
            }
            radioButtonId?.let { radioGroup.check(it) }
        }

        return dialogView
    }

    // Create a category chip
    private fun createCategoryChip(category: String): Chip {
        return Chip(requireContext()).apply {
            text = category
            isCheckable = true
            isChecked = selectedCategories.contains(category)
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedCategories.add(category)
                } else {
                    selectedCategories.remove(category)
                }
            }
        }
    }

    /**
     * Loads initial recipe data
     * TODO: Replace with actual API call
     * TODO: Should add loading state while it fetch data
     * TODO: Add error handling
     */
    private fun loadInitialData() {
        allRecipes.clear()
        allRecipes.addAll(getSampleData())
        recipeAdapter.submitList(allRecipes.toList())
    }

    /**
     * Filters recipes based on search query
     * TODO: move filtering logic to a different classssss
     * TODO: Add support for additional search criteria
     */
    private fun filterRecipes(query: String) {
        val filteredList = if (query.isEmpty()) {
            allRecipes
        } else {
            allRecipes.filter { recipe ->
                recipe.name.contains(query, ignoreCase = true) ||
                        recipe.category.contains(query, ignoreCase = true)
            }
        }
        recipeAdapter.submitList(filteredList)
    }

    /**
     * Applies selected filters to the recipe list
     * TODO: maybe add support for multiple filter combinations
     * TODO: Add filter persistence
     */
    private fun applyFilters() {
        val filteredList = allRecipes.filter { recipe ->
            val categoryMatch = if (selectedCategories.isEmpty()) {
                true
            } else {
                recipe.category.lowercase() in selectedCategories.map { it.lowercase() }
            }

            val difficultyMatch = if (selectedDifficulty == null) {
                true
            } else {
                recipe.difficulty == selectedDifficulty
            }

            categoryMatch && difficultyMatch
        }
        recipeAdapter.submitList(filteredList)
    }

    private fun clearFilters() {
        selectedCategories.clear()
        selectedDifficulty = null
        recipeAdapter.submitList(allRecipes)
        binding.searchBar.text?.clear()
    }

    /**
     * sample data for testing
     * TODO: Remove this when actual data source is implemented
     */
    private fun getSampleData(): List<Recipe> {
        return listOf(
            Recipe("Recipe 1", R.drawable.ic_recipe_placeholder, "breakfast", "Easy", "10 min", "2 servings", 4.5f, "NYU0SE8g35T3Kgjg9Gki1mFdhoW2"),
            Recipe("Recipe 2", R.drawable.ic_recipe_placeholder, "lunch", "Medium", "30 min", "4 servings", 4.2f, "NYU0SE8g35T3Kgjg9Gki1mFdhoW2"),
            Recipe("Recipe 3", R.drawable.ic_recipe_placeholder, "dinner", "Hard", "45 min", "5 servings", 3.8f, "NYU0SE8g35T3Kgjg9Gki1mFdhoW2"),
            Recipe("Recipe 4", R.drawable.ic_recipe_placeholder, "dinner", "Hard", "45 min", "5 servings", 3.8f,""),
            Recipe("Recipe 5", R.drawable.ic_recipe_placeholder, "dinner", "Hard", "45 min", "5 servings", 3.8f, ""),
            Recipe("Recipe 6", R.drawable.ic_recipe_placeholder, "dinner", "Hard", "45 min", "5 servings", 3.8f, "")
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