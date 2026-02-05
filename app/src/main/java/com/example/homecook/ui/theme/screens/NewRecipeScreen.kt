package com.example.homecook.ui.theme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.homecook.features.newrecipe.IngredientDraft
import com.example.homecook.features.newrecipe.NewRecipeViewModel
import com.example.homecook.features.newrecipe.StepDraft

@Composable
fun NewRecipeScreen(vm: NewRecipeViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()

    var title by remember { mutableStateOf("") }

    // Ingredient input fields
    var ingName by remember { mutableStateOf("") }
    var ingQtyText by remember { mutableStateOf("") }
    var ingUnit by remember { mutableStateOf("pcs") }
    val ingredients = remember { mutableStateListOf<IngredientDraft>() }

    // Step input field
    var stepText by remember { mutableStateOf("") }
    val steps = remember { mutableStateListOf<StepDraft>() }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            title = ""
            ingName = ""
            ingQtyText = ""
            ingUnit = "pcs"
            ingredients.clear()

            stepText = ""
            steps.clear()

            vm.consumeSaved()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .padding(bottom = 80.dp) // helps avoid bottom bar overlap
    ) {
        Text("New Recipe")

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            singleLine = true
        )

        // ---------- INGREDIENTS ----------
        Spacer(Modifier.height(16.dp))
        Text("Ingredients")

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = ingName,
            onValueChange = { ingName = it },
            label = { Text("Ingredient name") },
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = ingQtyText,
                onValueChange = { ingQtyText = it },
                label = { Text("Qty") },
                singleLine = true
            )
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = ingUnit,
                onValueChange = { ingUnit = it },
                label = { Text("Unit") },
                singleLine = true
            )
        }

        Spacer(Modifier.height(8.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                vm.clearError()

                val name = ingName.trim()
                val qty = ingQtyText.trim().toDoubleOrNull()
                val unit = ingUnit.trim().ifBlank { "pcs" }

                when {
                    name.isEmpty() -> return@Button
                    qty == null || qty <= 0.0 -> return@Button
                    else -> {
                        ingredients.add(IngredientDraft(name = name, quantity = qty, unit = unit))
                        ingName = ""
                        ingQtyText = ""
                        ingUnit = unit
                    }
                }
            },
            enabled = !state.isSaving
        ) {
            Text("Add ingredient")
        }

        Spacer(Modifier.height(10.dp))

        if (ingredients.isEmpty()) {
            Text("No ingredients added yet.")
        } else {
            ingredients.forEachIndexed { index, ing ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${index + 1}. ${ing.name} — ${ing.quantity} ${ing.unit}")
                    TextButton(onClick = { ingredients.removeAt(index) }) {
                        Text("Remove")
                    }
                }
            }
        }

        // ---------- STEPS ----------
        Spacer(Modifier.height(18.dp))
        Text("Steps")

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = stepText,
            onValueChange = { stepText = it },
            label = { Text("Step ${steps.size + 1}") },
            minLines = 2
        )

        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    vm.clearError()
                    val s = stepText.trim()
                    if (s.isNotEmpty()) {
                        steps.add(StepDraft(description = s))
                        stepText = ""
                    }
                },
                enabled = !state.isSaving
            ) {
                Text("Add step")
            }

            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    vm.save(
                        title = title,
                        ingredients = ingredients.toList(),
                        steps = steps.toList()
                    )
                },
                enabled = !state.isSaving
            ) {
                Text(if (state.isSaving) "Saving..." else "Finish & Save")
            }
        }

        Spacer(Modifier.height(10.dp))

        if (steps.isEmpty()) {
            Text("No steps added yet.")
        } else {
            steps.forEachIndexed { index, s ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${index + 1}. ${s.description}")
                    TextButton(onClick = { steps.removeAt(index) }) {
                        Text("Remove")
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        state.error?.let { Text(it) }

        if (state.saved) {
            Spacer(Modifier.height(8.dp))
            Text("Saved ✅")
        }
    }
}
