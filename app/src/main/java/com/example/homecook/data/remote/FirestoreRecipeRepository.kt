package com.example.homecook.data.remote

import android.util.Log
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
        val dto = this.toObject(RecipeDto::class.java) ?: return null
        dto.id = this.id
        return dto
    }

    fun observeAllRecipes(): Flow<List<RecipeDto>> = callbackFlow {
        val reg = paths.recipes()
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    // ✅ expected during logout -> do not crash UI
                    Log.w("FirestoreRecipeRepo", "observeAllRecipes error", err)
                    trySend(emptyList())
                    close() // close normally (no exception)
                    return@addSnapshotListener
                }

                val list = snap?.documents?.mapNotNull { it.toRecipeDto() } ?: emptyList()
                trySend(list)
            }

        awaitClose { reg.remove() }
    }

    fun observeRecipeById(recipeId: String): Flow<RecipeDto?> = callbackFlow {
        val reg = paths.recipes()
            .document(recipeId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    // ✅ expected during logout -> do not crash UI
                    Log.w("FirestoreRecipeRepo", "observeRecipeById error recipeId=$recipeId", err)
                    trySend(null)
                    close()
                    return@addSnapshotListener
                }

                trySend(snap?.toRecipeDto())
            }

        awaitClose { reg.remove() }
    }

    suspend fun getRecipeOnce(recipeId: String): RecipeDto? {
        val snap = paths.recipes().document(recipeId).get().await()
        val dto = snap.toObject(RecipeDto::class.java) ?: return null
        dto.id = snap.id
        return dto
    }

    suspend fun saveRecipe(recipe: RecipeDto) {
        val now = System.currentTimeMillis()
        val uid = paths.uid()

        val recipeId = if (recipe.id.isBlank()) paths.recipes().document().id else recipe.id

        val payload = recipe.copy(
            id = "",
            authorUid = if (recipe.authorUid.isBlank()) uid else recipe.authorUid,
            createdAt = if (recipe.createdAt == 0L) now else recipe.createdAt,
            updatedAt = now
        )

        paths.recipes().document(recipeId).set(payload, SetOptions.merge()).await()
    }

    suspend fun setMarked(recipeId: String, marked: Boolean) {
        paths.recipes().document(recipeId)
            .set(
                mapOf(
                    "isMarked" to marked,
                    "marked" to marked, // optional compatibility
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
            .await()
    }

    // ✅ Delete private only (keeps shared recipe online)
    suspend fun deletePrivateOnly(recipeId: String) {
        paths.recipes().document(recipeId).delete().await()
    }

    // ✅ Delete private + delete shared (if sharedId exists)
    suspend fun deletePrivateAndShared(recipeId: String) {
        val snap = paths.recipes().document(recipeId).get().await()
        val recipe = snap.toRecipeDto()

        val sharedId = recipe?.sharedId?.trim().orEmpty()
        if (sharedId.isNotBlank()) {
            paths.sharedRecipes().document(sharedId).delete().await()
        }

        paths.recipes().document(recipeId).delete().await()
    }

    // Optional helper: unshare but keep private
    suspend fun unshareKeepPrivate(recipeId: String) {
        val snap = paths.recipes().document(recipeId).get().await()
        val recipe = snap.toRecipeDto()
        val sharedId = recipe?.sharedId?.trim().orEmpty()

        if (sharedId.isNotBlank()) {
            paths.sharedRecipes().document(sharedId).delete().await()
            paths.recipes().document(recipeId)
                .set(mapOf("sharedId" to ""), SetOptions.merge())
                .await()
        }
    }
}
