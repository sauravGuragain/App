package com.fmcg.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fmcg.app.presentation.auth.LoginScreen
import com.fmcg.app.presentation.common.AdminHomeScreen
import com.fmcg.app.presentation.common.DeliveryHomeScreen
import com.fmcg.app.presentation.common.MarketingHomeScreen

@Composable
fun FmcgNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Destinations.LOGIN) {

        composable(Destinations.LOGIN) {
            LoginScreen(
                onLoggedIn = { role ->
                    navController.navigate(Destinations.homeFor(role)) {
                        popUpTo(Destinations.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Destinations.MARKETING_HOME) {
            MarketingHomeScreen(onLoggedOut = { backToLogin(navController) })
        }
        composable(Destinations.DELIVERY_HOME) {
            DeliveryHomeScreen(onLoggedOut = { backToLogin(navController) })
        }
        composable(Destinations.ADMIN_HOME) {
            AdminHomeScreen(onLoggedOut = { backToLogin(navController) })
        }
    }
}

private fun backToLogin(navController: NavHostController) {
    navController.navigate(Destinations.LOGIN) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}
