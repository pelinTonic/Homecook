package com.example.homecook.navigation

sealed class MainTab(val route: String, val label: String) {
    object NewRecipe : MainTab("tab_new_recipe", "New")
    object AllRecipes : MainTab("tab_all_recipes", "Recipes")
    object ShoppingList : MainTab("tab_shopping_list", "Shop")
    object Pantry : MainTab("tab_pantry", "Pantry")
    object Shared : MainTab("tab_shared", "Shared")
}
