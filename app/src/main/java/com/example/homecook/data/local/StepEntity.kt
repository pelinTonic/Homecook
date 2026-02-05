package com.example.homecook.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "steps")
data class StepEntity(
    @PrimaryKey val id: String,
    val recipeId: String,
    val stepNumber: Int,
    val description: String,
    val timeMinutes: Int? = null
)
