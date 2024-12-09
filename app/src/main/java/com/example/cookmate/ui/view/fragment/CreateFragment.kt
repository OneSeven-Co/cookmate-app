package com.example.cookmate.ui.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.cookmate.R
import com.example.cookmate.data.model.Ingredient
import com.example.cookmate.data.model.Recipe
import com.example.cookmate.databinding.FragmentCreateBinding
import com.example.cookmate.databinding.ItemIngredientBinding
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateFragment : Fragment() {
    private var _binding: FragmentCreateBinding? = null
    private val binding get() = _binding!!
    private val selectedCategories = mutableListOf<String>()
    private var selectedDifficulty: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoryDropdown()
        setupDifficultyDropdown()
        setupAddIngredientButton()
        setupImageUpload()
        setupSaveRecipeButton()
        setupSaveAndPublishButton()
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
        addNewIngredientField()
    }

    // Add new ingredient input field
    private fun addNewIngredientField() {
        val ingredientView = layoutInflater.inflate(R.layout.item_ingredient, null)
        val ingredientBinding = ItemIngredientBinding.bind(ingredientView)

        // Setup remove button
        ingredientBinding.removeIngredientButton.setOnClickListener {
            binding.ingredientsContainer.removeView(ingredientView)
        }

        binding.ingredientsContainer.addView(ingredientView)
    }
     // Collect ingredients from input fields
    private fun collectIngredients(): List<Ingredient> {
        val ingredients = mutableListOf<Ingredient>()
        for (i in 0 until binding.ingredientsContainer.childCount) {
            val ingredientView = binding.ingredientsContainer.getChildAt(i)
            val ingredientBinding = ItemIngredientBinding.bind(ingredientView)

            val name = ingredientBinding.ingredientNameInput.text.toString()
            val quantity = ingredientBinding.ingredientQuantityInput.text.toString()

            if (name.isNotBlank() && quantity.isNotBlank()) {
                ingredients.add(Ingredient(quantity, name))
            }
        }
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
        // Create recipe object
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
                recipeDescription = "Food",
                calories = 120f,
                fat = 12f to "grams",
                carbs = 12f to "grams",
                protein = 12f to "grams"
            )

            storeRecipe(recipe)
        }
    }

    private fun storeRecipe(recipe: Recipe) {
        val firestore = FirebaseFirestore.getInstance()

        val recipeData = hashMapOf(
            "title" to recipe.title,
            "ingredients" to recipe.ingredients,
            "preparationSteps" to recipe.preparationSteps,
            "cookingTime" to recipe.cookingTime,
            "prepTime" to recipe.prepTime,
            "servingSize" to recipe.servingSize,
            "categories" to recipe.categories,
            "difficulty" to recipe.difficulty,
            "isDraft" to recipe.isDraft,
            "authorId" to recipe.authorId
        )

        firestore.collection("recipes")
            .add(recipeData)
            .addOnSuccessListener { documentReference ->
                // Handle success
                Log.d("Firestore", "Recipe added with ID: ${documentReference.id}")
                Toast.makeText(
                    context,
                    if (recipe.isDraft) "Recipe saved as draft" else "Recipe published successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                // Handle failure
                Log.w("Firestore", "Error adding recipe", e)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}