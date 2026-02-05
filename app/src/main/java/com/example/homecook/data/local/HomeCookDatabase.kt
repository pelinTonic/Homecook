package com.example.homecook.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.homecook.data.local.dao.*

@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        StepEntity::class,
        PantryItemEntity::class,
        ShoppingItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HomeCookDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun stepDao(): StepDao
    abstract fun pantryDao(): PantryDao
    abstract fun shoppingDao(): ShoppingDao
}
