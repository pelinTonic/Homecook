package com.example.homecook.data.remote.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class PantryItemDto(
    @get:Exclude @set:Exclude
    var id: String = "",

    var name: String = "",
    var quantity: Double? = null,
    var unit: String = "",
    var updatedAt: Long = 0L
)
