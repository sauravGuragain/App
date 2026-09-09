package com.fmcg.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fmcg.app.presentation.admin.AdminHomeScreen
import com.fmcg.app.presentation.admin.AdminReportsScreen
import com.fmcg.app.presentation.admin.users.UserManagementScreen
import com.fmcg.app.presentation.auth.LoginScreen
import com.fmcg.app.presentation.common.camera.CameraCaptureScreen
import com.fmcg.app.presentation.common.map.LocationPickerScreen
import com.fmcg.app.presentation.delivery.DeliveryDetailScreen
import com.fmcg.app.presentation.delivery.DeliveryHomeScreen
import com.fmcg.app.presentation.marketing.MarketingHomeScreen
import com.fmcg.app.presentation.marketing.route.RouteMapScreen
import com.fmcg.app.presentation.order.OrderCreateScreen
import com.fmcg.app.presentation.order.OrderDetailScreen
import com.fmcg.app.presentation.order.OrderListScreen
import com.fmcg.app.presentation.splash.SplashDestination
import com.fmcg.app.presentation.splash.SplashScreen
import com.fmcg.app.presentation.store.StoreDetailScreen
import com.fmcg.app.presentation.store.StoreFormScreen
import com.fmcg.app.presentation.store.StoreListScreen

private const val PICKED_LOCATION_KEY = "picked_location"
private const val CAPTURED_PHOTO_KEY = "captured_photo"

@Composable
fun FmcgNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(onResolved = { dest ->
                val target = when (dest) {
                    is SplashDestination.Login -> Routes.LOGIN
                    is SplashDestination.Home -> Routes.homeFor(dest.role)
                }
                navController.navigate(target) { popUpTo(Routes.SPLASH) { inclusive = true } }
            })
        }

        composable(Routes.LOGIN) {
            LoginScreen(onLoggedIn = { role ->
                navController.navigate(Routes.homeFor(role)) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            })
        }

        composable(Routes.MARKETING_HOME) { entry ->
            val picked by entry.savedStateHandle
                .getStateFlow<String?>(PICKED_LOCATION_KEY, null).collectAsState()
            MarketingHomeScreen(
                onLoggedOut = { toLogin(navController) },
                onViewRoute = { navController.navigate(Routes.MARKETING_ROUTE_MAP) },
                onPinLocation = { navController.navigate(Routes.LOCATION_PICKER) },
                onStores = { navController.navigate(Routes.STORE_LIST) },
                onOrders = { navController.navigate(Routes.orderList(null)) },
                pickedLocation = picked,
                onPickedConsumed = { entry.savedStateHandle[PICKED_LOCATION_KEY] = null },
            )
        }

        composable(Routes.MARKETING_ROUTE_MAP) {
            RouteMapScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.LOCATION_PICKER) {
            LocationPickerScreen(
                onBack = { navController.popBackStack() },
                onConfirm = { lat, lng ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle?.set(PICKED_LOCATION_KEY, "$lat, $lng")
                    navController.popBackStack()
                },
            )
        }

        composable(Routes.STORE_LIST) {
            StoreListScreen(
                onBack = { navController.popBackStack() },
                onCreate = { navController.navigate(Routes.storeForm(null)) },
                onOpen = { id -> navController.navigate(Routes.storeDetail(id)) },
            )
        }

        composable(
            Routes.STORE_DETAIL,
            arguments = listOf(navArgument("storeId") { type = NavType.IntType }),
        ) {
            StoreDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Routes.storeForm(id)) },
                onNewOrder = { id -> navController.navigate(Routes.orderCreate(id)) },
                onOpenOrder = { id -> navController.navigate(Routes.orderDetail(id)) },
            )
        }

        composable(
            Routes.STORE_FORM,
            arguments = listOf(
                navArgument("storeId") { type = NavType.IntType; defaultValue = -1 }
            ),
        ) { entry ->
            val picked by entry.savedStateHandle
                .getStateFlow<String?>(PICKED_LOCATION_KEY, null).collectAsState()
            StoreFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                onPickLocation = { navController.navigate(Routes.LOCATION_PICKER) },
                pickedLocation = picked,
                onPickedConsumed = { entry.savedStateHandle[PICKED_LOCATION_KEY] = null },
            )
        }

        composable(
            Routes.ORDER_LIST,
            arguments = listOf(navArgument("storeId") { type = NavType.IntType; defaultValue = -1 }),
        ) {
            OrderListScreen(
                onBack = { navController.popBackStack() },
                onCreate = { navController.navigate(Routes.orderCreate(null)) },
                onOpen = { id -> navController.navigate(Routes.orderDetail(id)) },
            )
        }

        composable(
            Routes.ORDER_DETAIL,
            arguments = listOf(navArgument("orderId") { type = NavType.IntType }),
        ) {
            OrderDetailScreen(onBack = { navController.popBackStack() })
        }

        composable(
            Routes.ORDER_CREATE,
            arguments = listOf(navArgument("storeId") { type = NavType.IntType; defaultValue = -1 }),
        ) {
            OrderCreateScreen(
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack() },
            )
        }

        composable(Routes.ADMIN_HOME) {
            AdminHomeScreen(
                onReports = { navController.navigate(Routes.ADMIN_REPORTS) },
                onUsers = { navController.navigate(Routes.ADMIN_USERS) },
                onStores = { navController.navigate(Routes.STORE_LIST) },
                onOrders = { navController.navigate(Routes.orderList(null)) },
                onLoggedOut = { toLogin(navController) },
            )
        }

        composable(Routes.ADMIN_REPORTS) {
            AdminReportsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ADMIN_USERS) {
            UserManagementScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.DELIVERY_HOME) {
            DeliveryHomeScreen(
                onOpen = { id -> navController.navigate(Routes.deliveryDetail(id)) },
                onLoggedOut = { toLogin(navController) },
            )
        }

        composable(
            Routes.DELIVERY_DETAIL,
            arguments = listOf(navArgument("deliveryId") { type = NavType.IntType }),
        ) { entry ->
            val photo by entry.savedStateHandle
                .getStateFlow<String?>(CAPTURED_PHOTO_KEY, null).collectAsState()
            DeliveryDetailScreen(
                onBack = { navController.popBackStack() },
                onCapture = { navController.navigate(Routes.CAMERA_CAPTURE) },
                capturedPhotoPath = photo,
                onPhotoConsumed = { entry.savedStateHandle[CAPTURED_PHOTO_KEY] = null },
            )
        }

        composable(Routes.CAMERA_CAPTURE) {
            CameraCaptureScreen(
                onCaptured = { path ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle?.set(CAPTURED_PHOTO_KEY, path)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() },
            )
        }
    }
}

private fun toLogin(navController: NavHostController) {
    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
}
