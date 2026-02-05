package com.example.homecook.data.remote.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class IngredientDto(
    var name: String = "",
    var quantity: Double = 0.0,
    var unit: String = ""
)

@IgnoreExtraProperties
data class StepDto(
    var number: Int = 0,
    var description: String = "",
    var timeMinutes: Int? = null
)

@IgnoreExtraProperties
data class RecipeDto(
    @get:Exclude @set:Exclude
    var id: String = "",

    var title: String = "",
    var createdAt: Long = 0L,
    var updatedAt: Long = 0L,

    var isMarked: Boolean = false,

    var ingredients: List<IngredientDto> = emptyList(),
    var steps: List<StepDto> = emptyList(),

    var authorUid: String = ""
)
