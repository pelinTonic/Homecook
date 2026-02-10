package com.example.homecook.ui.theme.screens

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.homecook.auth.AuthViewModel
import com.example.homecook.navigation.MainRoutes
import com.example.homecook.navigation.MainTab
import com.example.homecook.navigation.Routes

@Composable
fun MainScreen(
    rootNavController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val tabNavController = rememberNavController()

    val tabs = listOf(
        MainTab.NewRecipe to Icons.Filled.Add,
        MainTab.AllRecipes to Icons.Filled.List,
        MainTab.ShoppingList to Icons.Filled.ShoppingCart,
        MainTab.Pantry to Icons.Filled.Person,
        MainTab.Shared to Icons.Filled.Share
    )

    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentTitle = tabs.firstOrNull { it.first.route == currentRoute }?.first?.label ?: "HomeCook"

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text(currentTitle) },
                actions = {
                    TextButton(
                        onClick = {
                            authViewModel.logout()
                            rootNavController.navigate(Routes.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    ) {
                        Text("Logout", color = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigation(
                modifier = Modifier.navigationBarsPadding()
            ) {
                tabs.forEach { (tab, icon) ->
                    BottomNavigationItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            tabNavController.navigate(tab.route) {
                                popUpTo(tabNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(icon, contentDescription = tab.label) },
                        label = {
                            Text(
                                text = tab.label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 10.sp
                            )
                        },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = MainTab.NewRecipe.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MainTab.NewRecipe.route) { NewRecipeScreen() }

            composable(MainTab.AllRecipes.route) {
                AllRecipesScreen(
                    onOpenRecipe = { id ->
                        // ✅ Navigate to root-level details screen
                        rootNavController.navigate(MainRoutes.recipeDetails(id))
                    }
                )
            }

            composable(MainTab.ShoppingList.route) { ShoppingListScreen() }
            composable(MainTab.Pantry.route) { PantryScreen() }
            composable(MainTab.Shared.route) { SharedRecipesScreen() }
        }
    }
}
