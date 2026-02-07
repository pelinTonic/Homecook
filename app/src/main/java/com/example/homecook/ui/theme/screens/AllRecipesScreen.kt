package com.example.homecook.ui.theme.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.homecook.features.allrecipes.AllRecipesViewModel
import com.example.homecook.data.remote.model.RecipeDto

@Composable
fun AllRecipesScreen(
    onOpenRecipe: (String) -> Unit,
    vm: AllRecipesViewModel = viewModel()
) {
    val recipes by vm.recipes.collectAsState(initial = emptyList())

    var pendingDelete by remember { mutableStateOf<RecipeDto?>(null) }

    pendingDelete?.let { r ->
        val isShared = r.sharedId.isNotBlank()

        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete recipe?") },
            text = {
                if (isShared) {
                    Text("This recipe is shared. Do you want to delete it only from your private recipes, or also remove it from Shared Recipes?")
                } else {
                    Text("Do you want to delete \"${r.title}\" or keep it?")
                }
            },
            confirmButton = {
                if (isShared) {
                    // "Delete private + shared"
                    TextButton(onClick = {
                        vm.deletePrivateAndShared(r.id)
                        pendingDelete = null
                    }) { Text("Delete private + shared") }
                } else {
                    // "Delete"
                    TextButton(onClick = {
                        vm.deletePrivateOnly(r.id)
                        pendingDelete = null
                    }) { Text("Delete") }
                }
            },
            dismissButton = {
                if (isShared) {
                    Row {
                        // "Delete private only"
                        TextButton(onClick = {
                            vm.deletePrivateOnly(r.id)
                            pendingDelete = null
                        }) { Text("Delete private only") }

                        // "Keep"
                        TextButton(onClick = { pendingDelete = null }) { Text("Keep") }
                    }
                } else {
                    TextButton(onClick = { pendingDelete = null }) { Text("Keep") }
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        Text("All Recipes")

        if (recipes.isEmpty()) {
            Text("No recipes yet.")
        } else {
            recipes.forEach { r ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenRecipe(r.id) }
                    ) {
                        Text(r.title)
                        Text("${r.ingredients.size} ingredients • ${r.steps.size} steps")
                        if (r.sharedId.isNotBlank()) {
                            Text("Shared")
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    Checkbox(
                        checked = r.isMarked,
                        onCheckedChange = { checked -> vm.setMarked(r.id, checked) }
                    )

                    IconButton(onClick = { pendingDelete = r }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete recipe")
                    }
                }
            }
        }
    }
}
