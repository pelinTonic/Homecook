package com.example.homecook.data.remote.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ShoppingItemDto(
    @get:Exclude @set:Exclude
    var id: String = "",

    var name: String = "",
    var quantity: Double? = null,
    var unit: String = "",
    var isChecked: Boolean = false,
    var isExcluded: Boolean = false,
    var updatedAt: Long = 0L
)
