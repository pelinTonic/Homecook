package com.example.homecook.features.recipedetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.homecook.data.remote.FirestoreRecipeRepository
import com.example.homecook.data.remote.FirestoreSharedRecipesRepository
import kotlinx.coroutines.launch

class RecipeDetailsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FirestoreRecipeRepository()
    private val sharedRepo = FirestoreSharedRecipesRepository()

    fun observe(recipeId: String) = repo.observeRecipeById(recipeId)

    fun share(recipeId: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                sharedRepo.sharePrivateRecipe(recipeId)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to share.")
            }
        }
    }

    fun unshare(recipeId: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                sharedRepo.unsharePrivateRecipe(recipeId)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to unshare.")
            }
        }
    }
}
