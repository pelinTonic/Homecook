package com.example.homecook.data.remote

import com.example.homecook.data.remote.model.RecipeDto
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRecipeRepository(
    private val paths: FirestorePaths = FirestorePaths()
) {

    private fun DocumentSnapshot.toRecipeDto(): RecipeDto? {
        val title = getString("title") ?: ""
        val createdAt = getLong("createdAt") ?: 0L
        val updatedAt = getLong("updatedAt") ?: 0L
        val isMarked = getBoolean("isMarked") ?: false
        val authorUid = getString("authorUid") ?: ""

        // ingredients
        val ingredientsAny = get("ingredients") as? List<*>
        val ingredients = ingredientsAny?.mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            val name = m["name"] as? String ?: return@mapNotNull null
            val quantity = (m["quantity"] as? Number)?.toDouble() ?: 0.0
            val unit = m["unit"] as? String ?: ""
            com.example.homecook.data.remote.model.IngredientDto(name = name, quantity = quantity, unit = unit)
        } ?: emptyList()

        // steps
        val stepsAny = get("steps") as? List<*>
        val steps = stepsAny?.mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            val number = (m["number"] as? Number)?.toInt() ?: 0
            val description = m["description"] as? String ?: ""
            val timeMinutes = (m["timeMinutes"] as? Number)?.toInt()
            com.example.homecook.data.remote.model.StepDto(number = number, description = description, timeMinutes = timeMinutes)
        } ?: emptyList()

        return RecipeDto(
            id = id,
            title = title,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isMarked = isMarked,
            ingredients = ingredients,
            steps = steps,
            authorUid = authorUid
        )
    }


    fun observeAllRecipes(): Flow<List<RecipeDto>> = callbackFlow {
        val reg = paths.recipes()
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { it.toRecipeDto() } ?: emptyList()
                trySend(list)
            }

        awaitClose { reg.remove() }
    }

    /**
     * NOTE:
     * whereEqualTo("isMarked", true) + orderBy("createdAt") often requires a composite index.
     * To avoid that, we query only on isMarked and sort client-side.
     */
    fun observeMarkedRecipes(): Flow<List<RecipeDto>> = callbackFlow {
        val reg = paths.recipes()
            .whereEqualTo("isMarked", true)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                val list = snap?.documents
                    ?.mapNotNull { it.toRecipeDto() }
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()

                trySend(list)
            }

        awaitClose { reg.remove() }
    }

    fun observeRecipeById(recipeId: String): Flow<RecipeDto?> = callbackFlow {
        val reg = paths.recipes()
            .document(recipeId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                trySend(snap?.toRecipeDto())
            }

        awaitClose { reg.remove() }
    }

    suspend fun saveRecipe(recipe: RecipeDto) {
        val now = System.currentTimeMillis()
        val uid = paths.uid()

        val recipeId = if (recipe.id.isBlank()) {
            paths.recipes().document().id
        } else {
            recipe.id
        }

        val payload = recipe.copy(
            id = "", // docId is the id
            authorUid = if (recipe.authorUid.isBlank()) uid else recipe.authorUid,
            createdAt = if (recipe.createdAt == 0L) now else recipe.createdAt,
            updatedAt = now
        )

        paths.recipes().document(recipeId).set(payload).await()
    }

    suspend fun setMarked(recipeId: String, marked: Boolean) {
        paths.recipes().document(recipeId)
            .set(
                mapOf(
                    "isMarked" to marked,
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
            .await()
    }

    suspend fun deleteRecipe(recipeId: String) {
        paths.recipes().document(recipeId).delete().await()
    }
}
