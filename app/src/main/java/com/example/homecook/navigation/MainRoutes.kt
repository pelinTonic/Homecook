package com.example.homecook.navigation

object MainRoutes {
    const val RECIPE_ID_ARG = "recipeId"
    const val RECIPE_DETAILS = "recipe_details"

    // route pattern for NavHost
    val RECIPE_DETAILS_ROUTE = "$RECIPE_DETAILS/{$RECIPE_ID_ARG}"

    // route builder for navigation
    fun recipeDetails(recipeId: String) = "$RECIPE_DETAILS/$recipeId"
}
