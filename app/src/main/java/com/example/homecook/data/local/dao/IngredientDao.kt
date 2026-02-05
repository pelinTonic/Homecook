package com.example.homecook.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.homecook.data.local.IngredientEntity

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients WHERE recipeId = :recipeId")
    suspend fun getForRecipe(recipeId: String): List<IngredientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<IngredientEntity>)

    @Query("DELETE FROM ingredients WHERE recipeId = :recipeId")
    suspend fun deleteForRecipe(recipeId: String)
}
