package com.example.homecook.ui.theme.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.homecook.features.shopping.ShoppingListViewModel

@Composable
fun ShoppingListScreen(vm: ShoppingListViewModel = viewModel()) {
    val items by vm.items.collectAsState()

    LaunchedEffect(Unit) { vm.sync() }

    val visibleItems = items.filter { !it.isExcluded }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        Text("Shopping List")

        if (visibleItems.isEmpty()) {
            Text(
                "Mark recipes to generate a shopping list.",
                modifier = Modifier.padding(top = 12.dp)
            )
        } else {
            visibleItems.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.isChecked,
                        onCheckedChange = { vm.toggleChecked(item.id, item.isChecked) }
                    )

                    val qty = item.quantity?.let { " $it" } ?: ""
                    val unit = item.unit.takeIf { it.isNotBlank() }?.let { " $it" } ?: ""
                    Text(
                        text = "${item.name}$qty$unit",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )

                    TextButton(
                        onClick = { vm.removeItem(item.id) },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Remove")
                    }
                }
            }
        }
    }
}
