package com.example.homecook.features.shared

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.homecook.data.remote.FirestoreSharedRecipesRepository
import kotlinx.coroutines.launch

class SharedRecipeDetailsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FirestoreSharedRecipesRepository()

    fun observe(sharedId: String) = repo.observeSharedRecipeById(sharedId)

    fun importToMyRecipes(
        sharedId: String,
        onDone: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val newPrivateId = repo.importSharedToMyRecipes(sharedId)
                onDone(newPrivateId)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to save recipe.")
            }
        }
    }
}
