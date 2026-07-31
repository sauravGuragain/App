package com.fmcg.app.presentation.delivery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.fmcg.app.domain.model.Delivery

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryHomeScreen(
    onOpen: (Int) -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: DeliveryHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My deliveries") },
                actions = {
                    IconButton(onClick = { viewModel.logout(); onLoggedOut() }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log out")
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when {
                state.isLoading -> CircularProgressIndicator()
                state.error != null -> Text(state.error!!)
                state.deliveries.isEmpty() -> Text("No deliveries assigned")
                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(state.deliveries, key = { it.id }) { d ->
                        DeliveryRow(d) { onOpen(d.id) }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun DeliveryRow(delivery: Delivery, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp)) {
        Text("Order #${delivery.orderId}", style = MaterialTheme.typography.titleMedium)
        Text(delivery.status.label, style = MaterialTheme.typography.bodySmall)
    }
}
