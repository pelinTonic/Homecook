package com.example.homecook.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val isMarked: Boolean = false,
    val isShared: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
