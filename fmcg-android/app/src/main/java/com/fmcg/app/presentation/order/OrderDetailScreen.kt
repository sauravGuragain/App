package com.fmcg.app.presentation.order

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.fmcg.app.domain.model.OrderItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OrderDetailScreen(
    onBack: () -> Unit,
    viewModel: OrderDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.order?.let { "Order #${it.id}" } ?: "Order") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            val order = state.order
            when {
                state.isLoading -> CircularProgressIndicator()
                order == null -> Text(state.error ?: "Order unavailable")
                else -> Column(
                    Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Status: ${order.status.label}",
                        style = MaterialTheme.typography.titleMedium)

                    HorizontalDivider()
                    order.items.forEach { item ->
                        ItemRow(item, state.productNames[item.productId] ?: "Product #${item.productId}")
                    }
                    HorizontalDivider()

                    Text("Subtotal: ${order.subtotal}")
                    Text("Discount: ${order.totalDiscount}")
                    Text("Total: ${order.total}",
                        style = MaterialTheme.typography.titleLarge)
                    order.notes?.let { Text("Notes: $it") }

                    if (state.isAdmin && order.status.allowedNext().isNotEmpty()) {
                        Text("Change status:",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(top = 12.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            order.status.allowedNext().forEach { next ->
                                AssistChip(
                                    onClick = { viewModel.changeStatus(next) },
                                    label = { Text(next.label) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemRow(item: OrderItem, name: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(name, style = MaterialTheme.typography.bodyLarge)
        Text(
            "${item.quantity} × ${item.unitPrice}" +
                (if (item.discount != "0" && item.discount != "0.00") "  − ${item.discount}" else "") +
                "  =  ${item.lineTotal}",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
