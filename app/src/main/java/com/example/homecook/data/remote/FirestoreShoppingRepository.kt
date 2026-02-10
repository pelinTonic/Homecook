package com.example.homecook.data.remote

import android.util.Log
import com.example.homecook.data.remote.model.RecipeDto
import com.example.homecook.data.remote.model.ShoppingItemDto
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreShoppingRepository(
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
                    // ✅ expected during logout -> do not crash UI
                    Log.w("FirestoreShoppingRepo", "observeShoppingItems error", err)
                    trySend(emptyList())
                    close()
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

    // ✅ NEW: hide/remove item from list (without it coming back on sync)
    suspend fun setExcluded(itemId: String, excluded: Boolean) {
        paths.shopping().document(itemId)
            .set(
                mapOf(
                    "isExcluded" to excluded,
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
            .await()
    }

    /**
     * Rebuilds shopping list from marked recipes:
     * - Sum quantities by (name + unit)
     * - Preserve existing isChecked + isExcluded
     * - Auto-check if pantry contains item
     * - Delete shopping items that are no longer needed (when recipes are unmarked)
     */
    suspend fun syncFromMarkedRecipes() {
        val markedSnap = paths.recipes()
            .whereEqualTo("isMarked", true)
            .get()
            .await()

        val markedRecipes = markedSnap.documents.mapNotNull { it.toRecipeDto() }

        // Pantry keys (stable ids like tomato__pcs)
        val pantrySnap = paths.pantry().get().await()
        val pantryKeys = pantrySnap.documents.map { it.id }.toSet()

        data class Agg(val name: String, val unit: String, val qty: Double?)
        val grouped = mutableMapOf<String, Agg>()

        markedRecipes.flatMap { it.ingredients }.forEach { ing ->
            val name = ing.name.trim()
            val unit = ing.unit.trim()
            if (name.isBlank()) return@forEach

            val key = makeStableKey(name, unit)
            val existing = grouped[key]
            val newQty = (existing?.qty ?: 0.0) + ing.quantity

            grouped[key] = Agg(name = name, unit = unit, qty = newQty)
        }

        val desiredKeys = grouped.keys

        // ✅ Preserve both checked + excluded
        data class ExistingState(val checked: Boolean, val excluded: Boolean)

        val existingSnap = paths.shopping().get().await()
        val existingState = existingSnap.documents.associate { doc ->
            val checked = doc.getBoolean("isChecked") ?: false
            val excluded = doc.getBoolean("isExcluded") ?: false
            doc.id to ExistingState(checked, excluded)
        }
        val existingKeys = existingState.keys

        val batch = db.batch()

        grouped.forEach { (key, agg) ->
            val docRef = paths.shopping().document(key)

            val prev = existingState[key]
            val preserveChecked = prev?.checked ?: false
            val preserveExcluded = prev?.excluded ?: false

            val autoChecked = pantryKeys.contains(key)
            val finalChecked = autoChecked || preserveChecked

            val payload = mapOf(
                "name" to agg.name,
                "unit" to agg.unit,
                "quantity" to agg.qty,
                "isChecked" to finalChecked,
                "isExcluded" to preserveExcluded, // ✅ important
                "updatedAt" to System.currentTimeMillis()
            )

            batch.set(docRef, payload, SetOptions.merge())
        }

        // Remove items no longer needed (e.g., recipe unmarked)
        (existingKeys - desiredKeys).forEach { oldKey ->
            batch.delete(paths.shopping().document(oldKey))
        }

        batch.commit().await()
    }

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
