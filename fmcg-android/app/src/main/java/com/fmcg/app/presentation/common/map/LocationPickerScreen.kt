package com.fmcg.app.presentation.common.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.osmdroid.util.GeoPoint

/**
 * Tap-to-pin location picker. Reused by store creation (Phase 8): the caller
 * gets back the chosen coordinates via [onConfirm].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(
    onBack: () -> Unit,
    onConfirm: (lat: Double, lng: Double) -> Unit,
    initialCenter: GeoPoint = GeoPoint(27.7172, 85.3240),
) {
    var selected by remember { mutableStateOf<GeoPoint?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pin store location") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OsmMap(
                modifier = Modifier.fillMaxWidth().weight(1f),
                center = initialCenter,
                onMapTap = { selected = it },
                markers = selected?.let {
                    listOf(MapMarker(it.latitude, it.longitude, "Selected"))
                } ?: emptyList(),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selected?.let { "Lat %.5f, Lng %.5f".format(it.latitude, it.longitude) }
                        ?: "Tap the map to place a pin",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = { selected?.let { onConfirm(it.latitude, it.longitude) } },
                    enabled = selected != null,
                ) { Text("Use location") }
            }
        }
    }
}
