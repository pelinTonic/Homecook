package com.example.homecook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.homecook.navigation.MainRoutes
import com.example.homecook.navigation.Routes
import com.example.homecook.ui.theme.HomeCookTheme
import com.example.homecook.ui.theme.screens.EditRecipeScreen
import com.example.homecook.ui.theme.screens.LoginScreen
import com.example.homecook.ui.theme.screens.MainScreen
import com.example.homecook.ui.theme.screens.RecipeDetailsScreen
import com.example.homecook.ui.theme.screens.RegisterScreen
import com.example.homecook.ui.theme.screens.SplashScreen
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        setContent {
            HomeCookTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Routes.Splash.route
                ) {
                    composable(Routes.Splash.route) {
                        SplashScreen(navController)
                    }

                    composable(Routes.Login.route) {
                        LoginScreen(navController)
                    }

                    composable(Routes.Register.route) {
                        RegisterScreen(navController)
                    }

                    composable(Routes.Main.route) {
                        MainScreen(navController)
                    }

                    // ✅ Recipe details
                    composable(
                        route = MainRoutes.RECIPE_DETAILS_ROUTE,
                        arguments = listOf(navArgument(MainRoutes.RECIPE_ID_ARG) {
                            type = NavType.StringType
                        })
                    ) { backStackEntry ->
                        val recipeId =
                            backStackEntry.arguments?.getString(MainRoutes.RECIPE_ID_ARG)!!
                        RecipeDetailsScreen(
                            recipeId = recipeId,
                            rootNavController = navController
                        )
                    }

                    // ✅ Edit recipe
                    composable(
                        route = MainRoutes.RECIPE_EDIT_ROUTE,
                        arguments = listOf(navArgument(MainRoutes.RECIPE_ID_ARG) {
                            type = NavType.StringType
                        })
                    ) { backStackEntry ->
                        val recipeId =
                            backStackEntry.arguments?.getString(MainRoutes.RECIPE_ID_ARG)!!
                        EditRecipeScreen(
                            recipeId = recipeId,
                            rootNavController = navController
                        )
                    }
                }
            }
        }
    }
}
