package com.example.homecook.ui.theme.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.homecook.features.pantry.PantryViewModel

@Composable
fun PantryScreen(vm: PantryViewModel = viewModel()) {
    val items by vm.items.collectAsState()

    var name by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("pcs") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        Text("My Pantry")

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            value = name,
            onValueChange = { name = it },
            label = { Text("Ingredient name") },
            singleLine = true
        )

        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = qty,
                onValueChange = { qty = it },
                label = { Text("Qty (optional)") },
                singleLine = true
            )
            OutlinedTextField(
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                value = unit,
                onValueChange = { unit = it },
                label = { Text("Unit") },
                singleLine = true
            )
        }

        Button(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            onClick = {
                vm.addItem(name, qty, unit)
                name = ""
                qty = ""
            }
        ) {
            Text("Add to pantry")
        }

        if (items.isEmpty()) {
            Text("No pantry items yet.", modifier = Modifier.padding(top = 12.dp))
        } else {
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    val qtyText = item.quantity?.let { " $it" } ?: ""
                    val unitText = item.unit.takeIf { it.isNotBlank() }?.let { " $it" } ?: ""
                    Text("• ${item.name}$qtyText$unitText", modifier = Modifier.weight(1f))

                    TextButton(onClick = { vm.removeItem(item.id) }) {
                        Text("Remove")
                    }
                }
            }
        }
    }
}
