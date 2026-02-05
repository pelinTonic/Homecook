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
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.homecook.features.allrecipes.AllRecipesViewModel

@Composable
fun AllRecipesScreen(
    onOpenRecipe: (String) -> Unit,
    vm: AllRecipesViewModel = viewModel()
) {
    val recipes by vm.recipes.collectAsState(initial = emptyList())

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
                        // debug line (optional): Text("Marked: ${r.isMarked}")
                    }

                    Spacer(Modifier.width(8.dp))

                    Checkbox(
                        checked = r.isMarked,
                        onCheckedChange = { checked ->
                            vm.setMarked(r.id, checked)
                        }
                    )
                }
            }
        }
    }
}
