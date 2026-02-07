package com.example.homecook.features.allrecipes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.homecook.data.remote.FirestoreRecipeRepository
import com.example.homecook.data.remote.FirestoreShoppingRepository
import kotlinx.coroutines.launch

class AllRecipesViewModel(app: Application) : AndroidViewModel(app) {
    private val recipeRepo = FirestoreRecipeRepository()
    private val shoppingRepo = FirestoreShoppingRepository()

    val recipes = recipeRepo.observeAllRecipes()

    fun setMarked(recipeId: String, marked: Boolean) {
        viewModelScope.launch {
            recipeRepo.setMarked(recipeId, marked)
            shoppingRepo.syncFromMarkedRecipes()
        }
    }

    fun deletePrivateOnly(recipeId: String) {
        viewModelScope.launch {
            recipeRepo.deletePrivateOnly(recipeId)
            shoppingRepo.syncFromMarkedRecipes()
        }
    }

    fun deletePrivateAndShared(recipeId: String) {
        viewModelScope.launch {
            recipeRepo.deletePrivateAndShared(recipeId)
            shoppingRepo.syncFromMarkedRecipes()
        }
    }
}
