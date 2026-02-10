package com.example.homecook.data.remote

import android.util.Log
import com.example.homecook.data.remote.model.PantryItemDto
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestorePantryRepository(
    private val paths: FirestorePaths = FirestorePaths(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun DocumentSnapshot.toPantryDto(): PantryItemDto? {
        val dto = this.toObject(PantryItemDto::class.java) ?: return null
        return dto.copy(id = this.id)
    }

    fun observePantryItems(): Flow<List<PantryItemDto>> = callbackFlow {
        val reg = paths.pantry()
            .orderBy("name")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    // ✅ expected during logout -> do not crash UI
                    Log.w("FirestorePantryRepo", "observePantryItems error", err)
                    trySend(emptyList())
                    close()
                    return@addSnapshotListener
                }

                val list = snap?.documents?.mapNotNull { it.toPantryDto() } ?: emptyList()
                trySend(list)
            }

        awaitClose { reg.remove() }
    }

    suspend fun upsert(name: String, quantity: Double?, unit: String) {
        val cleanName = name.trim()
        val cleanUnit = unit.trim()
        require(cleanName.isNotBlank()) { "Name is required" }

        val id = makeStableKey(cleanName, cleanUnit)
        val payload = mapOf(
            "name" to cleanName,
            "unit" to cleanUnit,
            "quantity" to quantity,
            "updatedAt" to System.currentTimeMillis()
        )

        paths.pantry().document(id).set(payload, SetOptions.merge()).await()
    }

    suspend fun delete(itemId: String) {
        paths.pantry().document(itemId).delete().await()
    }

    // MUST match the same logic as shopping repo, so IDs line up
    private fun makeStableKey(name: String, unit: String): String {
        fun norm(s: String) = s.trim().lowercase()
            .replace("\\s+".toRegex(), "_")
            .replace("[^a-z0-9_\\-]".toRegex(), "")

        val n = norm(name)
        val u = norm(unit)
        val key = "${n}__${u}".take(120)
        return if (key.isBlank()) "item" else key
    }
}
