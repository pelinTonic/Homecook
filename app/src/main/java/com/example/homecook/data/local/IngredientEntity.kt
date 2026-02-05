package com.example.homecook.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey val id: String,
    val recipeId: String,
    val name: String,
    val quantity: Double? = null,
    val unit: String? = null
)
