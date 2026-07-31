package com.fmcg.app.presentation.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
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
import com.fmcg.app.domain.model.DeliverySummary
import com.fmcg.app.domain.model.DistanceRow
import com.fmcg.app.domain.model.SalesRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(
    onBack: () -> Unit,
    viewModel: AdminReportsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            val bundle = state.bundle
            when {
                state.isLoading -> CircularProgressIndicator()
                bundle == null -> Text(state.error ?: "No report data")
                else -> LazyColumn(
                    Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item { SummaryCard(bundle.deliverySummary, bundle.newStoresCount) }
                    item { SalesCard("Sales by representative", bundle.salesByRep) }
                    item { SalesCard("Sales by product", bundle.salesByProduct) }
                    item { SalesCard("Sales by store", bundle.salesByStore) }
                    item { DistanceCard(bundle.distanceByRep) }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(summary: DeliverySummary, newStores: Int) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Overview", style = MaterialTheme.typography.titleMedium)
            Text("Deliveries — pending ${summary.pending}, out ${summary.outForDelivery}, " +
                "delivered ${summary.delivered}, failed ${summary.failed}")
            Text("Total stores: $newStores")
        }
    }
}

@Composable
private fun SalesCard(title: String, rows: List<SalesRow>) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (rows.isEmpty()) {
                Text("No data", style = MaterialTheme.typography.bodySmall)
            } else {
                rows.forEach { row ->
                    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text("${row.label} — ${row.total}",
                            style = MaterialTheme.typography.bodyLarge)
                        Text(row.secondary, style = MaterialTheme.typography.bodySmall)
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun DistanceCard(rows: List<DistanceRow>) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Distance travelled", style = MaterialTheme.typography.titleMedium)
            if (rows.isEmpty()) {
                Text("No data", style = MaterialTheme.typography.bodySmall)
            } else {
                rows.forEach { row ->
                    Text("${row.name} — %.2f km".format(row.distanceKm),
                        modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}
