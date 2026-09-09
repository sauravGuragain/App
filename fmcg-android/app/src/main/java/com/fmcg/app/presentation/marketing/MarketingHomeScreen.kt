package com.fmcg.app.presentation.marketing

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketingHomeScreen(
    onLoggedOut: () -> Unit,
    onViewRoute: () -> Unit,
    onPinLocation: () -> Unit,
    onStores: () -> Unit,
    onProducts: () -> Unit,
    onOrders: () -> Unit,
    pickedLocation: String?,
    onPickedConsumed: () -> Unit,
    viewModel: MarketingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val isTracking by viewModel.isTracking.collectAsState()
    val buffered by viewModel.bufferedCount.collectAsState()
    val synced by viewModel.syncedCount.collectAsState()
    val pendingSync by viewModel.pendingSync.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(pickedLocation) {
        pickedLocation?.let {
            snackbarHost.showSnackbar("Location pinned: $it")
            onPickedConsumed()
        }
    }

    val requiredPermissions = remember {
        buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val locationGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locationGranted) viewModel.startWorkday()
    }

    fun hasLocation(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    Scaffold(
        topBar = { TopAppBar(title = { Text("Marketing") }) },
        snackbarHost = { SnackbarHost(snackbarHost) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        if (isTracking) "Workday in progress" else "Workday not started",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        "Buffered points: $buffered   •   Synced this session: $synced",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    if (pendingSync > 0) {
                        Text(
                            "Pending sync: $pendingSync change(s) waiting for connection",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }

            if (isTracking) {
                Button(onClick = viewModel::endWorkday, modifier = Modifier.fillMaxWidth()) {
                    Text("End work day")
                }
            } else {
                Button(
                    onClick = {
                        if (hasLocation()) viewModel.startWorkday()
                        else launcher.launch(requiredPermissions)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Start work day") }
            }

            OutlinedButton(onClick = onStores, modifier = Modifier.fillMaxWidth()) {
                Text("My stores")
            }
            OutlinedButton(onClick = onOrders, modifier = Modifier.fillMaxWidth()) {
                Text("My orders")
            }
            OutlinedButton(onClick = onProducts, modifier = Modifier.fillMaxWidth()) {
                Text("Products")
            }
            OutlinedButton(onClick = onViewRoute, modifier = Modifier.fillMaxWidth()) {
                Text("View my route")
            }
            OutlinedButton(onClick = onPinLocation, modifier = Modifier.fillMaxWidth()) {
                Text("Pin a location")
            }
            OutlinedButton(onClick = { viewModel.logout(); onLoggedOut() },
                modifier = Modifier.fillMaxWidth()) {
                Text("Log out")
            }
        }
    }
}
