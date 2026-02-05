package com.example.homecook.data.remote

import com.example.homecook.data.remote.model.RecipeDto
import com.example.homecook.data.remote.model.ShoppingItemDto
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val paths: FirestorePaths = FirestorePaths(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun DocumentSnapshot.toShoppingDto(): ShoppingItemDto? {
        val dto = this.toObject(ShoppingItemDto::class.java) ?: return null
        return dto.copy(id = this.id)
    }

    private fun DocumentSnapshot.toRecipeDto(): RecipeDto? {
        val dto = this.toObject(RecipeDto::class.java) ?: return null
        return dto.copy(id = this.id)
    }

    fun observeShoppingItems(): Flow<List<ShoppingItemDto>> = callbackFlow {
        val reg = paths.shopping()
            .orderBy("name")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { it.toShoppingDto() } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun setChecked(itemId: String, checked: Boolean) {
        paths.shopping().document(itemId)
            .set(
                mapOf(
                    "isChecked" to checked,
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
            .await()
    }

    /**
     * Rebuilds shopping list from marked recipes:
     * - Sum quantities by (name + unit)
     * - Preserve existing isChecked if item already exists
     * - Delete shopping items that are no longer needed
     */
    suspend fun syncFromMarkedRecipes() {
        val markedSnap = paths.recipes()
            .whereEqualTo("isMarked", true)
            .get()
            .await()

        val markedRecipes = markedSnap.documents.mapNotNull { it.toRecipeDto() }

        // 1) Build desired items map: key -> (name, unit, quantitySum)
        data class Agg(val name: String, val unit: String, val qty: Double?)

        val grouped = mutableMapOf<String, Agg>()

        markedRecipes.flatMap { it.ingredients }.forEach { ing ->
            val name = ing.name.trim()
            val unit = ing.unit.trim()
            if (name.isBlank()) return@forEach

            val key = makeStableKey(name, unit)

            val existing = grouped[key]
            val newQty = (existing?.qty ?: 0.0) + ing.quantity

            grouped[key] = Agg(
                name = name,
                unit = unit,
                qty = newQty
            )
        }

        val desiredKeys = grouped.keys

        // 2) Read existing shopping items to preserve check state
        val existingSnap = paths.shopping().get().await()
        val existingChecked = existingSnap.documents.associate { doc ->
            val checked = doc.getBoolean("isChecked") ?: false
            doc.id to checked
        }
        val existingKeys = existingChecked.keys

        // 3) Batch write: upsert desired, delete removed
        val batch = db.batch()

        // upsert desired items
        grouped.forEach { (key, agg) ->
            val docRef = paths.shopping().document(key)
            val preserveChecked = existingChecked[key] ?: false

            val payload = mapOf(
                "name" to agg.name,
                "unit" to agg.unit,
                "quantity" to agg.qty,
                "isChecked" to preserveChecked,
                "updatedAt" to System.currentTimeMillis()
            )

            batch.set(docRef, payload, SetOptions.merge())
        }

        // delete items that are no longer needed
        (existingKeys - desiredKeys).forEach { oldKey ->
            batch.delete(paths.shopping().document(oldKey))
        }

        batch.commit().await()
    }

    // Stable Firestore docId based on ingredient name + unit
    // (So the same item keeps same ID across syncs and preserves checked state)
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
