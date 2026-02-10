package com.example.homecook.features.shared

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.homecook.data.remote.FirestoreSharedRecipesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SharedRecipesViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FirestoreSharedRecipesRepository()

    val sharedRecipes = repo.observeSharedRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun import(sharedId: String, onDone: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val newPrivateId = repo.importSharedToMyRecipes(sharedId)
                onDone(newPrivateId)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to import recipe.")
            }
        }
    }
}
