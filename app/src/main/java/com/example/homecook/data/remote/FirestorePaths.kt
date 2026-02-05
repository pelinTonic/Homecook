package com.example.homecook.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirestorePaths(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    fun uid(): String = auth.currentUser?.uid ?: error("Not logged in")

    fun userDoc() = db.collection("users").document(uid())
    fun recipes() = userDoc().collection("recipes")
    fun pantry() = userDoc().collection("pantry")
    fun shopping() = userDoc().collection("shopping")

    fun sharedRecipes() = db.collection("sharedRecipes")
}
