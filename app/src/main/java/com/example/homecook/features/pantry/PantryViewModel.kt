package com.example.homecook.features.pantry

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.homecook.data.remote.FirestorePantryRepository
import com.example.homecook.data.remote.FirestoreShoppingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PantryViewModel(app: Application) : AndroidViewModel(app) {

    private val pantryRepo = FirestorePantryRepository()
    private val shoppingRepo = FirestoreShoppingRepository()

    val items = pantryRepo.observePantryItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addItem(name: String, qtyText: String, unit: String) {
        val qty = qtyText.trim().toDoubleOrNull() // optional
        viewModelScope.launch {
            pantryRepo.upsert(name = name, quantity = qty, unit = unit)
            shoppingRepo.syncFromMarkedRecipes() // ✅ auto-check shopping list
        }
    }

    fun removeItem(id: String) {
        viewModelScope.launch {
            pantryRepo.delete(id)
            shoppingRepo.syncFromMarkedRecipes() // ✅ update auto-check
        }
    }
}
