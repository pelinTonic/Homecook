package com.example.homecook.features.allrecipes

import android.app.Application
import android.util.Log
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
            try {
                Log.d("AllRecipesVM", "Setting marked=$marked for recipeId=$recipeId")
                recipeRepo.setMarked(recipeId, marked)
                Log.d("AllRecipesVM", "Marked write SUCCESS for recipeId=$recipeId")

                shoppingRepo.syncFromMarkedRecipes()
                Log.d("AllRecipesVM", "Shopping sync SUCCESS")
            } catch (e: Exception) {
                Log.e("AllRecipesVM", "Mark/write FAILED", e)
            }
        }
    }
}
