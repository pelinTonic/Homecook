package com.example.homecook.data.remote

import android.util.Log
import com.example.homecook.data.remote.model.RecipeDto
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreSharedRecipesRepository(
    private val paths: FirestorePaths = FirestorePaths(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun DocumentSnapshot.toRecipeDto(): RecipeDto? {
        val dto = this.toObject(RecipeDto::class.java) ?: return null
        dto.id = this.id
        return dto
    }

    fun observeSharedRecipes(): Flow<List<RecipeDto>> = callbackFlow {
        val reg = paths.sharedRecipes()
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    // ✅ expected during logout -> do not crash UI
                    Log.w("FirestoreSharedRepo", "observeSharedRecipes error", err)
                    trySend(emptyList())
                    close()
                    return@addSnapshotListener
                }

                val list = snap?.documents?.mapNotNull { it.toRecipeDto() } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    fun observeSharedRecipeById(sharedId: String): Flow<RecipeDto?> = callbackFlow {
        val reg = paths.sharedRecipes()
            .document(sharedId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    // ✅ expected during logout -> do not crash UI
                    Log.w("FirestoreSharedRepo", "observeSharedRecipeById error sharedId=$sharedId", err)
                    trySend(null)
                    close()
                    return@addSnapshotListener
                }

                trySend(snap?.toRecipeDto())
            }
        awaitClose { reg.remove() }
    }

    /**
     * Share a private recipe:
     * - Copy it to sharedRecipes/{sharedId}
     * - Write sharedId back to users/{uid}/recipes/{recipeId}
     * If already shared, it updates the existing shared document.
     */
    suspend fun sharePrivateRecipe(privateRecipeId: String): String {
        val uid = paths.uid()
        val now = System.currentTimeMillis()

        val privateSnap = paths.recipes().document(privateRecipeId).get().await()
        val privateRecipe = privateSnap.toRecipeDto() ?: error("Recipe not found")

        val existingSharedId = privateRecipe.sharedId.trim()
        val sharedDocRef = if (existingSharedId.isNotBlank()) {
            paths.sharedRecipes().document(existingSharedId)
        } else {
            paths.sharedRecipes().document()
        }
        val sharedId = sharedDocRef.id

        val sharedPayload = privateRecipe.copy(
            id = "",
            isMarked = false,       // not used in public list
            authorUid = uid,        // ensure correct author
            sharedId = "",          // not needed inside shared doc
            createdAt = if (privateRecipe.createdAt == 0L) now else privateRecipe.createdAt,
            updatedAt = now
        )

        // Write/overwrite shared recipe (merge keeps flexibility)
        sharedDocRef.set(sharedPayload, SetOptions.merge()).await()

        // Mark private recipe as shared
        paths.recipes().document(privateRecipeId)
            .set(
                mapOf(
                    "sharedId" to sharedId,
                    "updatedAt" to now
                ),
                SetOptions.merge()
            )
            .await()

        return sharedId
    }

    suspend fun unsharePrivateRecipe(privateRecipeId: String) {
        val now = System.currentTimeMillis()

        val privateSnap = paths.recipes().document(privateRecipeId).get().await()
        val privateRecipe = privateSnap.toRecipeDto() ?: return

        val sharedId = privateRecipe.sharedId.trim()
        if (sharedId.isNotBlank()) {
            paths.sharedRecipes().document(sharedId).delete().await()
        }

        // Remove sharedId from private recipe
        paths.recipes().document(privateRecipeId)
            .set(
                mapOf(
                    "sharedId" to "",
                    "updatedAt" to now
                ),
                SetOptions.merge()
            )
            .await()
    }

    /**
     * Import a shared recipe into the current user's private recipes.
     * Returns the new private recipeId.
     */
    suspend fun importSharedToMyRecipes(sharedRecipeId: String): String {
        val uid = paths.uid()
        val now = System.currentTimeMillis()

        val snap = paths.sharedRecipes().document(sharedRecipeId).get().await()
        val shared = snap.toRecipeDto() ?: error("Shared recipe not found")

        val newDoc = paths.recipes().document()
        val newId = newDoc.id

        val payload = shared.copy(
            id = "",
            authorUid = uid,
            isMarked = false,
            sharedId = "",
            createdAt = now,
            updatedAt = now
        )

        newDoc.set(payload).await()
        return newId
    }
}
