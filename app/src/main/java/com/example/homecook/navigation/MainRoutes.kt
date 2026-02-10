package com.example.homecook.navigation

object MainRoutes {
    const val RECIPE_ID_ARG = "recipeId"

    // Screens
    const val RECIPE_DETAILS = "recipe_details"
    const val RECIPE_EDIT = "recipe_edit"

    // Route patterns for NavHost
    val RECIPE_DETAILS_ROUTE = "$RECIPE_DETAILS/{$RECIPE_ID_ARG}"
    val RECIPE_EDIT_ROUTE = "$RECIPE_EDIT/{$RECIPE_ID_ARG}"

    // Route builders for navigation
    fun recipeDetails(recipeId: String) = "$RECIPE_DETAILS/$recipeId"
    fun recipeEdit(recipeId: String) = "$RECIPE_EDIT/$recipeId"
}
