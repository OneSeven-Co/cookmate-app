package com.example.cookmate.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cookmate.data.model.Ingredient
import com.example.cookmate.data.model.Recipe
import com.example.cookmate.data.repository.RecipeRepository
import com.example.cookmate.utils.ImageUploadManager
import kotlinx.coroutines.launch

/**
 * ViewModel for handling recipe creation operations
 */
class CreateViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _ingredients = MutableLiveData<Result<List<Ingredient>>>()
    val ingredients: LiveData<Result<List<Ingredient>>> = _ingredients

    private val _saveResult = MutableLiveData<Result<String>>()
    val saveResult: LiveData<Result<String>> = _saveResult

    private val imageUploadManager = ImageUploadManager()
    private val _uploadResult = MutableLiveData<Result<String>>()
    val uploadResult: LiveData<Result<String>> = _uploadResult

    /**
     * Fetches all available ingredients
     */
    fun getAllIngredients() {
        viewModelScope.launch {
            recipeRepository.getAllIngredients().collect { result ->
                _ingredients.value = result
            }
        }
    }

    /**
     * Stores a new recipe
     * @param recipe The recipe to store
     */
    fun saveRecipe(recipe: Recipe) {
        viewModelScope.launch {
            recipeRepository.storeRecipe(recipe).collect { result ->
                _saveResult.value = result
            }
        }
    }

    /**
     * Uploads a recipe image to Firebase Storage
     * @param userId The ID of the user uploading the image
     * @param imageUri The URI of the image to upload
     */
    fun uploadImage(userId: String, imageUri: Uri) {
        viewModelScope.launch {
            _uploadResult.value = imageUploadManager.uploadRecipeImage(userId, imageUri)
        }
    }
} 