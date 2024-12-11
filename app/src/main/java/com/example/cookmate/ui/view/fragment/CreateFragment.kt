package com.example.cookmate.ui.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cookmate.R
import com.example.cookmate.data.model.Ingredient
import com.example.cookmate.data.model.Recipe
import com.example.cookmate.data.repository.RecipeRepository
import com.example.cookmate.databinding.FragmentCreateBinding
import com.example.cookmate.databinding.ItemIngredientBinding
import com.example.cookmate.ui.viewmodel.CreateViewModel
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth

class CreateFragment : Fragment() {
    private var _binding: FragmentCreateBinding? = null
    private val binding get() = _binding!!
    private val selectedCategories = mutableListOf<String>()
    private var selectedDifficulty: String? = null
    private var ingredientsList: List<Ingredient> = emptyList()

    private val viewModel: CreateViewModel by viewModels { 
        CreateViewModelFactory(RecipeRepository()) 
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupCategoryDropdown()
        setupDifficultyDropdown()
        setupAddIngredientButton()
        setupImageUpload()
        setupSaveRecipeButton()
        setupSaveAndPublishButton()
    }

    private fun setupObservers() {
        viewModel.ingredients.observe(viewLifecycleOwner) { result ->
            result.onSuccess { ingredients ->
                ingredientsList = ingredients
            }.onFailure { exception ->
                Toast.makeText(context, "Failed to load ingredients: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { _ ->
                Toast.makeText(context, "Recipe saved successfully!", Toast.LENGTH_LONG).show()
                // Optionally navigate away or clear form
            }.onFailure { exception ->
                Toast.makeText(context, "Failed to save recipe: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupImageUpload() {
        binding.uploadCard.setOnClickListener {
            // TODO: Implement image upload functionality
            Toast.makeText(context, "Image upload clicked", Toast.LENGTH_SHORT).show()
        }
    }

    // Set up category dropdown
    private fun setupCategoryDropdown() {
        val categories = listOf("Breakfast", "Lunch", "Dinner", "Desserts", "Appetizers", "Beverages")
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, categories)
        binding.categoryInput.setAdapter(adapter)

        binding.categoryInput.setOnItemClickListener { _, _, position, _ ->
            addCategoryChip(categories[position])
            binding.categoryInput.text.clear()
        }
    }

    // Add category chip
    private fun addCategoryChip(category: String) {
        if (selectedCategories.contains(category)) {
            Toast.makeText(context, "Category already added", Toast.LENGTH_SHORT).show()
            return
        }
        selectedCategories.add(category)

        val chip = Chip(context).apply {
            text = category
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                selectedCategories.remove(category)
                binding.chipGroupCategories.removeView(this)
            }
        }
        binding.chipGroupCategories.addView(chip)
    }

    // Set up difficulty dropdown
    private fun setupDifficultyDropdown() {
        val difficulties = listOf("Easy", "Medium", "Hard")
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, difficulties)
        binding.difficultyInput.setAdapter(adapter)

        binding.difficultyInput.setOnItemClickListener { _, _, position, _ ->
            addDifficultyChip(difficulties[position])
            binding.difficultyInput.text.clear()
        }
    }

    private fun addDifficultyChip(difficulty: String) {
        // Remove existing chip if any, since we only want one difficulty selected
        binding.chipGroupDifficulty.removeAllViews()
        selectedDifficulty = difficulty

        val chip = Chip(context).apply {
            text = difficulty
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                selectedDifficulty = null
                binding.chipGroupDifficulty.removeView(this)
            }
        }
        binding.chipGroupDifficulty.addView(chip)
    }

    private fun setupAddIngredientButton() {
        binding.addIngredientButton.setOnClickListener {
            addNewIngredientField()
        }
        
        // Load ingredients and add first field
        viewModel.getAllIngredients()
        addNewIngredientField()
    }

    // Add new ingredient input field
    private fun addNewIngredientField() {
        val ingredientView = layoutInflater.inflate(R.layout.item_ingredient, null)
        val ingredientBinding = ItemIngredientBinding.bind(ingredientView)

        // Setup remove button
        ingredientBinding.removeIngredientButton.setOnClickListener {
            binding.ingredientsContainer.removeView(ingredientView)
            binding.addIngredientButton.isEnabled = true
        }

        // Add the view
        binding.ingredientsContainer.addView(ingredientView)
        
        // Always enable the add button
        binding.addIngredientButton.isEnabled = true
    }

    // Collect ingredients from input fields
    private fun collectIngredients(): List<Ingredient> {
        val ingredients = mutableListOf<Ingredient>()
        for (i in 0 until binding.ingredientsContainer.childCount) {
            val ingredientView = binding.ingredientsContainer.getChildAt(i)
            val ingredientBinding = ItemIngredientBinding.bind(ingredientView)

            val name = ingredientBinding.ingredientNameInput.text.toString()
            val quantityString = ingredientBinding.ingredientQuantityInput.text.toString()
            
            // Skip empty fields
            if (name.isBlank() || quantityString.isBlank()) {
                continue
            }

            val quantity = quantityString.toFloatOrNull() ?: 0f
            
            // Try to find existing ingredient
            val currentIngredient = ingredientsList.find { 
                it.name.equals(name, ignoreCase = true) 
            }

            // Create ingredient whether it exists in the list or not
            ingredients.add(
                Ingredient(
                    amount = quantity,
                    unit = currentIngredient?.unit ?: "units", // Default unit if not found
                    name = name,
                    substitutes = currentIngredient?.substitutes ?: emptyList()
                )
            )
        }
        
        Log.d("ingredients", ingredients.toString())
        return ingredients
    }

    // Save recipe as draft
    private fun setupSaveRecipeButton() {
        binding.saveRecipeButton.setOnClickListener {
            saveRecipe(draft = true)
        }
    }

    // Save and publish recipe
    private fun setupSaveAndPublishButton() {
        binding.saveAndPublishButton.setOnClickListener {
            saveRecipe(draft = false)
        }
    }

    // Validate user inputs
    private fun validateInputs(): Boolean {
        var isValid = true
        val errorMessage = StringBuilder()

        if (binding.recipeTitleInput.text.isNullOrBlank()) {
            errorMessage.append("Title is required\n")
            isValid = false
        }

        if (binding.preparationStepsInput.text.isNullOrBlank()) {
            errorMessage.append("Preparation steps are required\n")
            isValid = false
        }

        if (binding.timeInput.text.isNullOrBlank()) {
            errorMessage.append("Cooking time is required\n")
            isValid = false
        }

        if (binding.prepTimeInput.text.isNullOrBlank()) {
            errorMessage.append("Prep time is required\n")
            isValid = false
        }

        if (binding.servingSizeInput.text.isNullOrBlank()) {
            errorMessage.append("Serving size is required\n")
            isValid = false
        }

        if (collectIngredients().isEmpty()) {
            errorMessage.append("At least one ingredient is required\n")
            isValid = false
        }

        if (selectedDifficulty == null) {
            errorMessage.append("Difficulty level is required\n")
            isValid = false
        }

        if (selectedCategories.isEmpty()) {
            errorMessage.append("At least one category is required")
            isValid = false
        }

        if (!isValid) {
            Toast.makeText(context, errorMessage.toString().trim(), Toast.LENGTH_LONG).show()
        }

        return isValid
    }

    private fun saveRecipe(draft: Boolean) {
        if (!validateInputs()) {
            return
        }

        val firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser
        
        if (user != null) {
            val recipe = Recipe(
                title = binding.recipeTitleInput.text.toString(),
                ingredients = collectIngredients(),
                preparationSteps = binding.preparationStepsInput.text.toString(),
                cookingTime = binding.timeInput.text.toString(),
                prepTime = binding.prepTimeInput.text.toString(),
                servingSize = binding.servingSizeInput.text.toString(),
                categories = selectedCategories.toList(),
                difficulty = selectedDifficulty!!,
                isDraft = draft,
                authorId = user.uid,
                imageRes = 0,
                rating = 5f,
                recipeDescription = binding.descriptionInput.text.toString(),
                calories = binding.caloriesInput.text.toString().toFloat() to "kcal",
                fat = binding.fatInput.text.toString().toFloat() to "Grams",
                carbs = binding.carbohydratesInput.text.toString().toFloat() to "Grams",
                protein = binding.proteinInput.text.toString().toFloat() to "Grams"
            )

            viewModel.saveRecipe(recipe)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * Factory for creating CreateViewModel instances
 */
class CreateViewModelFactory(
    private val recipeRepository: RecipeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateViewModel(recipeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}