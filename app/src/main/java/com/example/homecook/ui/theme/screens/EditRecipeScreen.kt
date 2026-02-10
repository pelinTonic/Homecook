package com.example.homecook.ui.theme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.homecook.data.remote.model.IngredientDto
import com.example.homecook.data.remote.model.StepDto
import com.example.homecook.features.editrecipe.EditRecipeViewModel

@Composable
fun EditRecipeScreen(
    recipeId: String,
    rootNavController: NavController,
    vm: EditRecipeViewModel = viewModel()
) {
    val recipe by vm.observe(recipeId).collectAsState(initial = null)

    var initialized by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }

    // ingredient draft inputs
    var ingName by remember { mutableStateOf("") }
    var ingQtyText by remember { mutableStateOf("") }
    var ingUnit by remember { mutableStateOf("pcs") }

    // step draft input
    var stepText by remember { mutableStateOf("") }

    val ingredients = remember { mutableStateListOf<IngredientDto>() }
    val steps = remember { mutableStateListOf<StepDto>() }

    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(recipe) {
        val r = recipe ?: return@LaunchedEffect
        if (!initialized) {
            initialized = true
            title = r.title
            ingredients.clear()
            ingredients.addAll(r.ingredients)
            steps.clear()
            steps.addAll(r.steps.sortedBy { it.number })
        }
    }

    fun addIngredient() {
        val name = ingName.trim()
        val qty = ingQtyText.trim().toDoubleOrNull()
        val unit = ingUnit.trim().ifBlank { "pcs" }

        if (name.isBlank() || qty == null || qty <= 0.0) return

        ingredients.add(IngredientDto(name = name, quantity = qty, unit = unit))
        ingName = ""
        ingQtyText = ""
        ingUnit = unit
    }

    fun addStep() {
        val s = stepText.trim()
        if (s.isBlank()) return
        steps.add(StepDto(number = steps.size + 1, description = s))
        stepText = ""
    }

    fun save() {
        error = null
        isSaving = true
        vm.saveEdits(
            recipeId = recipeId,
            title = title,
            ingredients = ingredients.toList(),
            steps = steps.toList(),
            onDone = {
                isSaving = false
                rootNavController.popBackStack() // back to details
            },
            onError = {
                isSaving = false
                error = it
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Recipe") },
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
                    TextButton(onClick = { if (!isSaving) save() }) {
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
            if (recipe == null && !initialized) {
                Text("Loading...")
                return@Column
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))
            Text("Ingredients")

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                value = ingName,
                onValueChange = { ingName = it },
                label = { Text("Ingredient name") },
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

            Button(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                onClick = { addIngredient() },
                enabled = !isSaving
            ) { Text("Add ingredient") }

            if (ingredients.isEmpty()) {
                Text("No ingredients yet.", modifier = Modifier.padding(top = 8.dp))
            } else {
                ingredients.forEachIndexed { index, ing ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${index + 1}. ${ing.name} — ${ing.quantity} ${ing.unit}",
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { ingredients.removeAt(index) }, enabled = !isSaving) {
                            Text("Remove")
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Text("Steps")

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                value = stepText,
                onValueChange = { stepText = it },
                label = { Text("Step ${steps.size + 1}") },
                minLines = 2
            )

            Button(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                onClick = { addStep() },
                enabled = !isSaving
            ) { Text("Add step") }

            if (steps.isEmpty()) {
                Text("No steps yet.", modifier = Modifier.padding(top = 8.dp))
            } else {
                steps.forEachIndexed { index, s ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${index + 1}. ${s.description}", modifier = Modifier.weight(1f))
                        TextButton(onClick = { steps.removeAt(index) }, enabled = !isSaving) {
                            Text("Remove")
                        }
                    }
                }
            }

            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it)
            }
        }
    }
}
