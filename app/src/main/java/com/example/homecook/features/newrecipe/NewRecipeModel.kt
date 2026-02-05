package com.example.homecook.features.newrecipe

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.homecook.data.remote.FirestoreRecipeRepository
import com.example.homecook.data.remote.model.IngredientDto
import com.example.homecook.data.remote.model.RecipeDto
import com.example.homecook.data.remote.model.StepDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IngredientDraft(val name: String, val quantity: Double, val unit: String)
data class StepDraft(val description: String)

data class NewRecipeUiState(
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

class NewRecipeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = FirestoreRecipeRepository()

    private val _uiState = MutableStateFlow(NewRecipeUiState())
    val uiState: StateFlow<NewRecipeUiState> = _uiState.asStateFlow()

    fun save(title: String, ingredients: List<IngredientDraft>, steps: List<StepDraft>) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) {
            _uiState.value = NewRecipeUiState(error = "Title is required.")
            return
        }
        if (ingredients.isEmpty()) {
            _uiState.value = NewRecipeUiState(error = "Add at least 1 ingredient.")
            return
        }
        if (steps.isEmpty()) {
            _uiState.value = NewRecipeUiState(error = "Add at least 1 step.")
            return
        }

        val dto = RecipeDto(
            title = cleanTitle,
            isMarked = false,
            ingredients = ingredients.map {
                IngredientDto(name = it.name.trim(), quantity = it.quantity, unit = it.unit.trim())
            },
            steps = steps.mapIndexed { idx, s ->
                StepDto(number = idx + 1, description = s.description.trim(), timeMinutes = null)
            }
        )

        _uiState.value = NewRecipeUiState(isSaving = true)

        viewModelScope.launch {
            try {
                repo.saveRecipe(dto)
                _uiState.value = NewRecipeUiState(saved = true)
            } catch (e: Exception) {
                _uiState.value = NewRecipeUiState(error = e.localizedMessage ?: "Save failed.")
            }
        }
    }

    fun consumeSaved() {
        _uiState.value = NewRecipeUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
