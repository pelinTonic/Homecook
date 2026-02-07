package com.example.homecook.data.remote.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class ShoppingItemDto(
    @get:Exclude @set:Exclude
    var id: String = "",

    var name: String = "",
    var quantity: Double? = null,
    var unit: String = "",

    @get:PropertyName("isChecked")
    @set:PropertyName("isChecked")
    var isChecked: Boolean = false,

    @get:PropertyName("isExcluded")
    @set:PropertyName("isExcluded")
    var isExcluded: Boolean = false,

    var updatedAt: Long = 0L
)
