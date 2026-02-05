package com.example.homecook.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.homecook.data.local.RecipeEntity
import kotlinx.coroutines.flow.Flow
import androidx.room.Transaction
import com.example.homecook.data.local.RecipeWithDetails


@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<RecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(recipe: RecipeEntity)

    @Query("UPDATE recipes SET isMarked = :marked WHERE id = :recipeId")
    suspend fun setMarked(recipeId: String, marked: Boolean)

    @Query("DELETE FROM recipes WHERE id = :recipeId")
    suspend fun delete(recipeId: String)

    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    fun observeAllWithDetails(): Flow<List<RecipeWithDetails>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE isMarked = 1 ORDER BY createdAt DESC")
    fun observeMarkedWithDetails(): Flow<List<RecipeWithDetails>>


    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :recipeId LIMIT 1")
    fun observeByIdWithDetails(recipeId: String): Flow<RecipeWithDetails?>

}

