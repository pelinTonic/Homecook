package com.example.homecook.ui.theme.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.homecook.features.shared.SharedRecipesViewModel

@Composable
fun SharedRecipesScreen(
    onOpenShared: (String) -> Unit,
    vm: SharedRecipesViewModel = viewModel()
) {
    val recipes by vm.sharedRecipes.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        Text("Shared Recipes")

        if (recipes.isEmpty()) {
            Text("No shared recipes yet.", modifier = Modifier.padding(top = 12.dp))
        } else {
            recipes.forEach { r ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clickable { onOpenShared(r.id) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(r.title)
                        Text("${r.ingredients.size} ingredients • ${r.steps.size} steps")
                    }
                }
            }
        }
    }
}
