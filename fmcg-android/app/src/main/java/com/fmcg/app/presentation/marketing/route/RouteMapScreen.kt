package com.fmcg.app.presentation.marketing.route

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fmcg.app.presentation.common.map.MapMarker
import com.fmcg.app.presentation.common.map.OsmMap
import org.osmdroid.util.GeoPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMapScreen(
    onBack: () -> Unit,
    viewModel: RouteViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My route — %.2f km".format(state.distanceKm)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when {
                state.isLoading -> CircularProgressIndicator()
                state.error != null -> Column0(state.error!!, viewModel::load)
                state.points.isEmpty() -> Text("No route recorded today yet")
                else -> {
                    val geo = state.points.map { GeoPoint(it.latitude, it.longitude) }
                    OsmMap(
                        modifier = Modifier.fillMaxSize(),
                        center = geo.first(),
                        routePoints = geo,
                        markers = listOf(
                            MapMarker(geo.first().latitude, geo.first().longitude, "Start"),
                            MapMarker(geo.last().latitude, geo.last().longitude, "Latest"),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun Column0(message: String, onRetry: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message)
        Button(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) { Text("Retry") }
    }
}
