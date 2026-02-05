package com.example.homecook.features.recipedetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.homecook.data.remote.FirestoreRecipeRepository

class RecipeDetailsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FirestoreRecipeRepository()
    fun observe(recipeId: String) = repo.observeRecipeById(recipeId)
}
