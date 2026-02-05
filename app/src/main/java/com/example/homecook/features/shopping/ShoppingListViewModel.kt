package com.example.homecook.features.shopping

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.homecook.data.remote.FirestoreShoppingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShoppingListViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = FirestoreShoppingRepository()

    val items = repo.observeShoppingItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleChecked(id: String, current: Boolean) {
        viewModelScope.launch {
            repo.setChecked(id, !current)
        }
    }

    fun sync() {
        viewModelScope.launch {
            repo.syncFromMarkedRecipes()
        }
    }
    fun removeItem(id: String) {
        viewModelScope.launch {
            repo.setExcluded(id, true)
        }
    }

}
