package com.example.homecook.ui.theme.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import com.example.homecook.features.pantry.PantryViewModel

@Composable
fun PantryScreen(vm: PantryViewModel = viewModel()) {
    val items by vm.items.collectAsState()

    var isExpanded by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var qtyText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("pcs") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        Text("My Pantry")
        Spacer(Modifier.height(12.dp))

        // ✅ Dropdown header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isExpanded) "Hide input" else "Add pantry item",
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null
            )
        }

        // ✅ Collapsible input area
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ingredient name") },
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Qty (optional)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        singleLine = true
                    )
                }

                Spacer(Modifier.height(10.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val cleanName = name.trim()
                        val cleanUnit = unit.trim().ifBlank { "pcs" }
                        if (cleanName.isBlank()) return@Button

                        // ✅ Add item, but DO NOT clear inputs and DO NOT collapse
                        vm.addItem(name = cleanName, qtyText = qtyText, unit = cleanUnit)
                    }
                ) {
                    Text("Add to pantry")
                }

                Spacer(Modifier.height(16.dp))
            }
        }

        // ✅ List
        if (items.isEmpty()) {
            Text("No pantry items yet.")
        } else {
            items.forEach { item ->
                val qty = item.quantity?.let { " $it" } ?: ""
                val u = item.unit.takeIf { it.isNotBlank() }?.let { " $it" } ?: ""

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.name}$qty$u",
                        modifier = Modifier.weight(1f)
                    )

                    TextButton(onClick = { vm.removeItem(item.id) }) {
                        Text("Remove")
                    }
                }
            }
        }
    }
}
