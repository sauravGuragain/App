package com.fmcg.app.presentation.order

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fmcg.app.domain.model.Product
import com.fmcg.app.domain.model.Store

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderCreateScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: OrderCreateViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.submitted) {
        if (state.submitted) {
            if (state.queuedOffline) snackbar.showSnackbar("Offline — order queued and will sync")
            onDone()
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New order") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                if (!state.lockedStore) {
                    StorePicker(
                        stores = state.stores,
                        selectedId = state.selectedStoreId,
                        onSelect = viewModel::setStore,
                    )
                } else {
                    val name = state.stores.firstOrNull { it.id == state.selectedStoreId }?.name
                    Text("Store: ${name ?: "#${state.selectedStoreId}"}",
                        style = MaterialTheme.typography.titleMedium)
                }
            }

            items(state.lines, key = { it.key }) { line ->
                LineCard(
                    line = line,
                    products = state.products,
                    onProduct = { p -> viewModel.selectProduct(line.key, p) },
                    onQty = { viewModel.updateLine(line.key, quantity = it) },
                    onPrice = { viewModel.updateLine(line.key, unitPrice = it) },
                    onDiscount = { viewModel.updateLine(line.key, discount = it) },
                    onRemove = { viewModel.removeLine(line.key) },
                )
            }

            item {
                TextButton(onClick = viewModel::addLine) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Add item")
                }
            }

            item {
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = viewModel::setNotes,
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                Text("Total: ${state.total}", style = MaterialTheme.typography.titleLarge)
            }

            item {
                Button(
                    onClick = viewModel::submit,
                    enabled = state.canSave && !state.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (state.isSaving) "Placing…" else "Place order") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StorePicker(stores: List<Store>, selectedId: Int?, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = stores.firstOrNull { it.id == selectedId }?.name ?: "Select store"
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Store") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            stores.forEach { s ->
                DropdownMenuItem(text = { Text(s.name) }, onClick = { onSelect(s.id); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LineCard(
    line: LineInput,
    products: List<Product>,
    onProduct: (Product) -> Unit,
    onQty: (String) -> Unit,
    onPrice: (String) -> Unit,
    onDiscount: (String) -> Unit,
    onRemove: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.weight(1f),
                ) {
                    OutlinedTextField(
                        value = line.productName.ifBlank { "Select product" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Product") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        products.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.name} (${p.defaultPrice})") },
                                onClick = { onProduct(p); expanded = false },
                            )
                        }
                    }
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Close, contentDescription = "Remove item")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = line.quantity, onValueChange = onQty, label = { Text("Qty") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(80.dp),
                )
                OutlinedTextField(
                    value = line.unitPrice, onValueChange = onPrice, label = { Text("Price") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = line.discount, onValueChange = onDiscount, label = { Text("Disc.") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.width(90.dp),
                )
            }
        }
    }
}
