package com.fmcg.app.presentation.common.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import org.osmdroid.util.GeoPoint

/**
 * Location picker with three ways to choose a point:
 *   - automatic GPS detection (runs on open, repeatable via "My location")
 *   - searching for a place by name
 *   - tapping the map, or typing coordinates directly
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(
    onBack: () -> Unit,
    onConfirm: (lat: Double, lng: Double) -> Unit,
    initialCenter: GeoPoint = GeoPoint(27.7172, 85.3240),
    viewModel: LocationPickerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showManualEntry by remember { mutableStateOf(false) }
    var latText by remember { mutableStateOf("") }
    var lngText by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) viewModel.detectCurrentLocation()
    }

    // Detect on open; ask for permission the first time.
    LaunchedEffect(Unit) {
        if (viewModel.hasLocationPermission()) {
            viewModel.detectCurrentLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pin location") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Search for a place or address") },
                singleLine = true,
                trailingIcon = {
                    if (state.isSearching) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (state.results.isNotEmpty()) {
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    LazyColumn(Modifier.heightIn(max = 220.dp)) {
                        items(state.results) { result ->
                            Text(
                                text = result.label,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onResultSelected(result) }
                                    .padding(12.dp),
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }

            OsmMap(
                modifier = Modifier.fillMaxWidth().weight(1f),
                center = initialCenter,
                onMapTap = { viewModel.onMapTap(it.latitude, it.longitude) },
                markers = state.picked?.let {
                    listOf(MapMarker(it.lat, it.lng, it.label ?: "Selected"))
                } ?: emptyList(),
                recenterTo = state.recenterTo?.let { GeoPoint(it.lat, it.lng) },
            )

            LaunchedEffect(state.recenterTo) {
                if (state.recenterTo != null) viewModel.onRecenterConsumed()
            }

            Column(Modifier.fillMaxWidth().padding(16.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = {
                            if (viewModel.hasLocationPermission()) {
                                viewModel.detectCurrentLocation()
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                    )
                                )
                            }
                        },
                        enabled = !state.isLocating,
                    ) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null)
                        Text(
                            if (state.isLocating) "  Locating…" else "  My location",
                        )
                    }
                    TextButton(onClick = { showManualEntry = !showManualEntry }) {
                        Text(if (showManualEntry) "Hide coordinates" else "Enter coordinates")
                    }
                }

                if (showManualEntry) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = latText,
                            onValueChange = { latText = it },
                            label = { Text("Latitude") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = lngText,
                            onValueChange = { lngText = it },
                            label = { Text("Longitude") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    TextButton(
                        onClick = { viewModel.onCoordinatesEntered(latText, lngText) },
                        modifier = Modifier.padding(top = 4.dp),
                    ) { Text("Use these coordinates") }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = state.picked?.let { p ->
                            p.label?.takeIf { it != "Selected" }?.let { "$it\n" }.orEmpty() +
                                "Lat %.5f, Lng %.5f".format(p.lat, p.lng)
                        } ?: "Detecting your location — or tap the map to place a pin",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                    )
                    Button(
                        onClick = { state.picked?.let { onConfirm(it.lat, it.lng) } },
                        enabled = state.picked != null,
                    ) { Text("Use location") }
                }
            }
        }
    }
}
