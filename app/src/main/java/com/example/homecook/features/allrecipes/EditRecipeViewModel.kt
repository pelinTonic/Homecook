package com.example.homecook.features.editrecipe

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.homecook.data.remote.FirestoreRecipeRepository
import com.example.homecook.data.remote.model.IngredientDto
import com.example.homecook.data.remote.model.StepDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class EditRecipeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FirestoreRecipeRepository()

    fun observe(recipeId: String) = repo.observeRecipeById(recipeId)

    fun saveEdits(
        recipeId: String,
        title: String,
        ingredients: List<IngredientDto>,
        steps: List<StepDto>,
        onDone: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val current = repo.observeRecipeById(recipeId) // flow
                // We'll just fetch the latest snapshot once via Firestore directly:
                // easiest clean approach: call repo.saveRecipe with id set; Firestore merges doc.
                val now = System.currentTimeMillis()

                val renumberedSteps = steps.mapIndexed { idx, s ->
                    s.copy(number = idx + 1)
                }

                repo.saveRecipe(
                    com.example.homecook.data.remote.model.RecipeDto(
                        id = recipeId,
                        title = title.trim(),
                        ingredients = ingredients,
                        steps = renumberedSteps,
                        updatedAt = now
                    )
                )

                onDone()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to save changes.")
            }
        }
    }
}
