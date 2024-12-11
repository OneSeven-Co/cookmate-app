package com.example.cookmate.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cookmate.data.model.Recipe
import com.example.cookmate.data.repository.RecipeRepository
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _favorites = MutableLiveData<Result<List<Recipe>>>()
    val favorites: LiveData<Result<List<Recipe>>> = _favorites

    fun loadFavorites(userId: String) {
        viewModelScope.launch {
            recipeRepository.getFavoriteRecipes(userId).collect { result ->
                _favorites.value = result
            }
        }
    }
}

class FavoritesViewModelFactory(
    private val recipeRepository: RecipeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(recipeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 