package com.example.homecook.data

import com.example.homecook.data.local.HomeCookDatabase
import com.example.homecook.data.local.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ShoppingRepository(private val db: HomeCookDatabase) {

    fun observeShoppingList(): Flow<List<ShoppingItemEntity>> =
        db.shoppingDao().observeAll()

    fun observeGeneratedShoppingItems(): Flow<List<ShoppingItemEntity>> {
        return db.recipeDao().observeMarkedWithDetails().map { markedRecipes ->
            // Flatten all ingredients from marked recipes
            val allIngredients = markedRecipes.flatMap { it.ingredients }

            // Group by name+unit and sum quantities when possible
            val grouped = allIngredients.groupBy { "${it.name.trim().lowercase()}|${it.unit?.trim()?.lowercase() ?: ""}" }

            grouped.values.map { group ->
                val first = group.first()
                val sumQty = group.mapNotNull { it.quantity }.takeIf { it.isNotEmpty() }?.sum()

                ShoppingItemEntity(
                    id = UUID.randomUUID().toString(),
                    name = first.name.trim(),
                    quantity = sumQty,
                    unit = first.unit?.trim(),
                    isChecked = false
                )
            }.sortedBy { it.name.lowercase() }
        }
    }

    suspend fun writeGeneratedShoppingList(items: List<ShoppingItemEntity>) {
        db.shoppingDao().replaceAll(items)
    }

    suspend fun setChecked(id: String, checked: Boolean) {
        db.shoppingDao().setChecked(id, checked)
    }
}
