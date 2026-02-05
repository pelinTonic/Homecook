package com.example.homecook.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.homecook.data.local.StepEntity

@Dao
interface StepDao {
    @Query("SELECT * FROM steps WHERE recipeId = :recipeId ORDER BY stepNumber ASC")
    suspend fun getForRecipe(recipeId: String): List<StepEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<StepEntity>)

    @Query("DELETE FROM steps WHERE recipeId = :recipeId")
    suspend fun deleteForRecipe(recipeId: String)
}
