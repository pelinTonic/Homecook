package com.example.homecook.ui.theme.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.homecook.features.shared.SharedRecipeDetailsViewModel
import com.example.homecook.navigation.MainRoutes

@Composable
fun SharedRecipeDetailsScreen(
    sharedId: String,
    rootNavController: NavController,
    vm: SharedRecipeDetailsViewModel = viewModel()
) {
    val recipe by vm.observe(sharedId).collectAsState(initial = null)
    var error by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe?.title ?: "Shared Recipe") },
                navigationIcon = {
                    IconButton(onClick = { rootNavController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (isSaving) return@TextButton
                            error = null
                            isSaving = true
                            vm.importToMyRecipes(
                                sharedId = sharedId,
                                onDone = { newPrivateId ->
                                    isSaving = false
                                    // open the imported private recipe details
                                    rootNavController.navigate(MainRoutes.recipeDetails(newPrivateId))
                                },
                                onError = { msg ->
                                    isSaving = false
                                    error = msg
                                }
                            )
                        }
                    ) {
                        Text(if (isSaving) "Saving..." else "Save", color = Color.White)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp)
        ) {
            if (recipe == null) {
                Text("Loading...")
                return@Column
            }

            val r = recipe!!

            Text(r.title)

            error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it)
            }

            Spacer(Modifier.height(16.dp))
            Text("Ingredients")
            r.ingredients.forEachIndexed { index, ing ->
                Text("${index + 1}. ${ing.name} — ${ing.quantity} ${ing.unit}")
            }

            Spacer(Modifier.height(16.dp))
            Text("Steps")
            r.steps.sortedBy { it.number }.forEach { step ->
                Text("${step.number}. ${step.description}")
            }
        }
    }
}
