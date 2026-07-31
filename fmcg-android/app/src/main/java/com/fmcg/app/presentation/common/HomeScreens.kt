package com.fmcg.app.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Shared placeholder used by each role's home until its feature screens land
 * (marketing: Phase 6–9, delivery: Phase 10, admin: Phase 12). Demonstrates the
 * Scaffold + top bar + logout wiring the real screens will build on.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleHomeScaffold(
    title: String,
    subtitle: String,
    onLoggedOut: () -> Unit,
    session: SessionViewModel = hiltViewModel(),
) {
    Scaffold(topBar = { TopAppBar(title = { Text(title) }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(subtitle, style = MaterialTheme.typography.titleMedium)
            Text(
                "Feature screens arrive in upcoming phases.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            OutlinedButton(
                onClick = { session.logout(onLoggedOut) },
                modifier = Modifier.padding(top = 24.dp),
            ) { Text("Log out") }
        }
    }
}

@Composable
fun MarketingHomeScreen(onLoggedOut: () -> Unit) =
    RoleHomeScaffold("Marketing", "Welcome, field rep", onLoggedOut)

@Composable
fun DeliveryHomeScreen(onLoggedOut: () -> Unit) =
    RoleHomeScaffold("Deliveries", "Welcome, driver", onLoggedOut)

@Composable
fun AdminHomeScreen(onLoggedOut: () -> Unit) =
    RoleHomeScaffold("Admin", "Welcome, administrator", onLoggedOut)
