package com.fmcg.app.presentation.store

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onPickLocation: () -> Unit,
    pickedLocation: String?,
    onPickedConsumed: () -> Unit,
    viewModel: StoreFormViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    // Coordinates returned from the tap-to-pin picker ("lat, lng").
    LaunchedEffect(pickedLocation) {
        pickedLocation?.let { raw ->
            raw.split(",").map { it.trim().toDoubleOrNull() }.let { parts ->
                if (parts.size == 2 && parts[0] != null && parts[1] != null) {
                    viewModel.setPickedLocation(parts[0]!!, parts[1]!!)
                }
            }
            onPickedConsumed()
        }
    }
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }
    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditing) "Edit store" else "New store") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(state.name, viewModel::onName, label = { Text("Store name *") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.ownerName, viewModel::onOwner, label = { Text("Owner name") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.phone, viewModel::onPhone, label = { Text("Phone") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.address, viewModel::onAddress, label = { Text("Address") },
                modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.notes, viewModel::onNotes, label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth())

            OutlinedButton(onClick = onPickLocation, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (state.latitude != null)
                        "Location: %.5f, %.5f".format(state.latitude, state.longitude)
                    else "Pick location on map *"
                )
            }

            Button(
                onClick = viewModel::save,
                enabled = state.canSave && !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (state.isSaving) "Saving…" else "Save store") }
        }
    }
}
