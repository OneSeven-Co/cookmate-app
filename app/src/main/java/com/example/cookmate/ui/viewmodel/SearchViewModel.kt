package com.example.cookmate.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cookmate.data.model.Recipe
import com.example.cookmate.data.repository.RecipeRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for handling search screen recipe operations
 * @property recipeRepository Repository for recipe operations
 */
class SearchViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _recipes = MutableLiveData<Result<List<Recipe>>>()
    val recipes: LiveData<Result<List<Recipe>>> = _recipes

    fun loadPublishedRecipes() {
        viewModelScope.launch {
            recipeRepository.getAllPublishedRecipes().collect { result ->
                _recipes.value = result
            }
        }
    }
}

class SearchViewModelFactory(
    private val recipeRepository: RecipeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(recipeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}