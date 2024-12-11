package com.example.cookmate.ui.view.fragment

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog.Builder as MaterialAlertDialogBuilder
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.cookmate.R
import com.example.cookmate.data.model.Ingredient
import com.example.cookmate.data.model.Recipe
import com.example.cookmate.data.repository.RecipeRepository
import com.example.cookmate.databinding.FragmentCreateBinding
import com.example.cookmate.databinding.ItemIngredientBinding
import com.example.cookmate.ui.viewmodel.CreateViewModel
import com.example.cookmate.utils.PermissionsUtils
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.yalantis.ucrop.UCrop
import java.io.File

class CreateFragment : Fragment() {
    private var _binding: FragmentCreateBinding? = null
    private val binding get() = _binding!!
    private val selectedCategories = mutableListOf<String>()
    private var selectedDifficulty: String? = null
    private var ingredientsList: List<Ingredient> = emptyList()
    private var selectedImageUri: Uri? = null
    private var tempImageUri: Uri? = null
    private var isKeyboardVisible = false
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            tempImageUri = it
            startImageCropping(it)
        }
    }

    private val cropImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->
                val resultUri = UCrop.getOutput(intent)
                resultUri?.let { uri ->
                    selectedImageUri = uri
                    updateImagePreview(uri)
                }
            }
        } else {
            // If cropping failed or was cancelled, reset to default state
            resetImagePreview()
        }
    }

    private val viewModel: CreateViewModel by viewModels { 
        CreateViewModelFactory(RecipeRepository()) 
    }

    private val permissionLauncher = PermissionsUtils.createPermissionLauncher(this) {
        pickImage.launch("image/*")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackPressHandling()
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

        viewModel.uploadResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { url ->
                createAndSaveRecipe(FirebaseAuth.getInstance().currentUser!!.uid, url, false)
            }.onFailure { exception ->
                Toast.makeText(
                    context,
                    "Failed to upload image: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupImageUpload() {
        binding.uploadCard.setOnClickListener {
            if (PermissionsUtils.hasStoragePermission(requireContext())) {
                pickImage.launch("image/*")
            } else {
                permissionLauncher.launch(PermissionsUtils.getRequiredPermission())
            }
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

        val firebaseAuth = FirebaseAuth.getInstance().currentUser ?: return

        // If there's an image, upload it first then save recipe
        if (selectedImageUri != null) {
            viewModel.uploadImage(firebaseAuth.uid, selectedImageUri!!)
        } else {
            createAndSaveRecipe(firebaseAuth.uid, null, draft)
        }
    }

    private fun createAndSaveRecipe(userId: String, imageUrl: String?, draft: Boolean) {
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
            authorId = userId,
            imageRes = 0,
            imageUrl = imageUrl,
            rating = 5f,
            recipeDescription = binding.descriptionInput.text.toString(),
            calories = binding.caloriesInput.text.toString().toFloat() to "kcal",
            fat = binding.fatInput.text.toString().toFloat() to "Grams",
            carbs = binding.carbohydratesInput.text.toString().toFloat() to "Grams",
            protein = binding.proteinInput.text.toString().toFloat() to "Grams"
        )

        viewModel.saveRecipe(recipe)
    }

    /**
     * Starts the image cropping activity
     */
    private fun startImageCropping(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(
            File(requireContext().cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
        )

        val uCrop = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(16f, 9f)  // Match image view aspect ratio
            .withMaxResultSize(1920, 1080)

        cropImage.launch(uCrop.getIntent(requireContext()))
    }

    /**
     * Updates the image preview maintaining aspect ratio
     */
    private fun updateImagePreview(uri: Uri) {
        binding.uploadIcon.scaleType = ImageView.ScaleType.CENTER_CROP
        binding.uploadText.visibility = View.GONE  // Hide the text when image is selected
        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(binding.uploadIcon)
    }

    // Add function to reset image preview
    private fun resetImagePreview() {
        binding.uploadIcon.scaleType = ImageView.ScaleType.CENTER
        binding.uploadIcon.setImageResource(R.drawable.ic_upload)
        binding.uploadText.visibility = View.VISIBLE  // Show the text when no image
        selectedImageUri = null
        tempImageUri = null
    }

    /**
     * Sets up custom back press handling to handle keyboard first
     */
    private fun setupBackPressHandling() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    val currentFocus = requireActivity().currentFocus
                    
                    if (currentFocus != null && imm.isActive) {
                        // If keyboard is visible, hide it
                        imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
                        isKeyboardVisible = false
                    } else {
                        // If keyboard is not visible, show dialog to confirm exit
                        showExitConfirmationDialog()
                    }
                }
            }
        )
    }

    /**
     * Shows a dialog to confirm exiting the create screen
     */
    private fun showExitConfirmationDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Discard Changes?")
            .setMessage("Are you sure you want to exit? Any unsaved changes will be lost.")
            .setPositiveButton("Exit") { _, _ ->
                // Remove callback and allow normal back navigation
                requireActivity().onBackPressedDispatcher
                    .onBackPressed()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
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