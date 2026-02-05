package com.example.homecook.data

import com.example.homecook.data.local.HomeCookDatabase
import com.example.homecook.data.local.IngredientEntity
import com.example.homecook.data.local.RecipeEntity
import com.example.homecook.data.local.StepEntity
import kotlinx.coroutines.flow.Flow
import com.example.homecook.data.local.RecipeWithDetails

class RecipeRepository(private val db: HomeCookDatabase) {

    fun observeAllRecipes(): Flow<List<RecipeWithDetails>> =
        db.recipeDao().observeAllWithDetails()

    suspend fun saveRecipe(
        recipe: RecipeEntity,
        ingredients: List<IngredientEntity>,
        steps: List<StepEntity>
    ) {
        db.recipeDao().upsert(recipe)
        db.ingredientDao().deleteForRecipe(recipe.id)
        db.stepDao().deleteForRecipe(recipe.id)
        db.ingredientDao().upsertAll(ingredients)
        db.stepDao().upsertAll(steps)
    }
}


