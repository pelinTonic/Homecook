package com.example.homecook.ui.theme.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.homecook.features.recipedetails.RecipeDetailsViewModel

@Composable
fun RecipeDetailsScreen(
    recipeId: String,
    vm: RecipeDetailsViewModel = viewModel()
) {
    val recipe by vm.observe(recipeId).collectAsState(initial = null)

    Column(
        modifier = Modifier
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
