package com.fmcg.app.presentation.store

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fmcg.app.domain.model.Order
import com.fmcg.app.presentation.common.map.MapMarker
import com.fmcg.app.presentation.common.map.OsmMap
import org.osmdroid.util.GeoPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailScreen(
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    onNewOrder: (Int) -> Unit,
    onOpenOrder: (Int) -> Unit,
    viewModel: StoreDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.deleted) { if (state.deleted) onBack() }
    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.store?.name ?: "Store") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    state.store?.let { s ->
                        IconButton(onClick = { onEdit(s.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = viewModel::delete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            val s = state.store
            when {
                state.isLoading -> CircularProgressIndicator()
                s == null -> Text(state.error ?: "Store unavailable")
                else -> Column(
                    Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    s.ownerName?.let { Field("Owner", it) }
                    s.phone?.let { Field("Phone", it) }
                    s.address?.let { Field("Address", it) }
                    s.notes?.let { Field("Notes", it) }
                    Field("Coordinates", "%.5f, %.5f".format(s.latitude, s.longitude))
                    OsmMap(
                        modifier = Modifier.fillMaxWidth().height(240.dp),
                        center = GeoPoint(s.latitude, s.longitude),
                        markers = listOf(MapMarker(s.latitude, s.longitude, s.name)),
                    )
                    OutlinedButton(
                        onClick = { onNewOrder(s.id) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("New order for this store") }

                    Text("Order history", style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp))
                    if (state.orders.isEmpty()) {
                        Text("No orders yet", style = MaterialTheme.typography.bodySmall)
                    } else {
                        state.orders.forEach { order ->
                            OrderHistoryRow(order) { onOpenOrder(order.id) }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderHistoryRow(order: Order, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp)) {
        Text("Order #${order.id} — ${order.total}", style = MaterialTheme.typography.bodyLarge)
        Text(order.status.label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun Field(label: String, value: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
