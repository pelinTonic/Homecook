package com.example.homecook.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val quantity: Double? = null,
    val unit: String? = null,
    val isChecked: Boolean = false
)
