package com.fmcg.app.presentation.delivery

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fmcg.app.domain.model.DeliveryStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryDetailScreen(
    onBack: () -> Unit,
    onCapture: () -> Unit,
    capturedPhotoPath: String?,
    onPhotoConsumed: () -> Unit,
    viewModel: DeliveryDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(capturedPhotoPath) {
        capturedPhotoPath?.let { viewModel.onProofCaptured(it); onPhotoConsumed() }
    }
    LaunchedEffect(state.done) { if (state.done) onBack() }
    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.delivery?.let { "Order #${it.orderId}" } ?: "Delivery") },
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
            val d = state.delivery
            when {
                state.isLoading -> CircularProgressIndicator()
                d == null -> Text(state.error ?: "Delivery unavailable")
                else -> Column(
                    Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Status: ${d.status.label}", style = MaterialTheme.typography.titleMedium)
                    state.storeName?.let { Text("Store: $it") }

                    val lat = state.latitude
                    val lng = state.longitude
                    if (lat != null && lng != null) {
                        OutlinedButton(
                            onClick = {
                                val label = Uri.encode(state.storeName ?: "Delivery")
                                val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Navigate to store") }
                    }

                    when (d.status) {
                        DeliveryStatus.PENDING -> {
                            Button(
                                onClick = viewModel::startDelivery,
                                enabled = !state.isBusy,
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Start delivery") }
                        }
                        DeliveryStatus.OUT_FOR_DELIVERY -> {
                            OutlinedButton(onClick = onCapture, modifier = Modifier.fillMaxWidth()) {
                                Text(if (state.proofUrl != null) "Proof captured ✓" else "Capture proof photo")
                            }
                            OutlinedTextField(
                                value = state.notes,
                                onValueChange = viewModel::setNotes,
                                label = { Text("Delivery notes") },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Button(
                                onClick = viewModel::markDelivered,
                                enabled = !state.isBusy && state.proofUrl != null,
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Mark delivered") }
                            OutlinedButton(
                                onClick = viewModel::markFailed,
                                enabled = !state.isBusy,
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Mark failed") }
                        }
                        else -> {
                            d.proofPhotoUrl?.let { Text("Proof: $it") }
                            d.notes?.let { Text("Notes: $it") }
                        }
                    }
                }
            }
        }
    }
}
