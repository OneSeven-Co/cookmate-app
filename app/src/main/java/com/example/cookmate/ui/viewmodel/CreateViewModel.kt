package com.example.cookmate.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cookmate.data.model.Ingredient
import com.example.cookmate.data.model.Recipe
import com.example.cookmate.data.repository.RecipeRepository
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
} 