// NavGraph.kt
package com.example.colfi.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.colfi.ui.screens.*
import com.example.colfi.ui.viewmodel.*
//import com.example.colfi.data.repository.TableRepository
//import com.example.colfi.data.model.AppDatabase
import androidx.compose.ui.platform.LocalContext

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(navController: NavHostController) {
    /*val context = LocalContext.current
    val db = AppDatabase.getInstance(context) // Assuming you have a getInstance method
    val tableRepository = remember { TableRepository(db.tableDao()) }*/
    NavHost(
        navController = navController,
        startDestination = Screen.Loading.route
    ) {
        // 🔹 Loading
        composable(Screen.Loading.route) {
            val viewModel: LoadingViewModel = viewModel()
            LoadingScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Loading.route) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        // 🔹 Login  --- THIS BLOCK IS UPDATED ---
        composable(Screen.Login.route) {
            // LoginScreen now requires two navigation actions, so we provide both.
            LoginScreen(
                onNavigateToHome = { userName ->
                    // This is for a successful, real user login.
                    navController.navigate(Screen.CustomerHome.createRoute(userName)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateAsGuest = {
                    // This is for the "Continue as Guest" button.
                    navController.navigate(Screen.CustomerHome.createRoute("Guest")) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // 🔹 Register Screen --- THIS IS THE NEWLY ADDED BLOCK ---
        composable(Screen.Register.route) {
            RegisterScreen(
                // This allows the user to go back to the login screen after registering or by pressing a back button
                onNavigateBackToLogin = {
                    navController.popBackStack()
                }
            )
        }
        // 🔹 Customer Home
        composable(
            route = Screen.CustomerHome.route,
            arguments = listOf(navArgument("user_name") { type = NavType.StringType })
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("user_name") ?: "Guest"
            val viewModel: HomeViewModel = viewModel()
            CustomerHomeScreen(
                userName = userName,
                onNavigateToMenu = {
                    navController.navigate(Screen.Menu.createRoute(userName)) {
                        launchSingleTop = true
                    }
                },
                onNavigateToOrders = {
                    navController.navigate(Screen.Orders.createRoute(userName)) {
                        launchSingleTop = true
                    }
                },
                onNavigateToDineIn = {
                    navController.navigate(Screen.DineIn.createRoute(userName))
                },
                onNavigateToPickUp = {
                    navController.navigate(Screen.PickUp.createRoute(userName))
                },
                onNavigateToDelivery = {
                    navController.navigate(Screen.Delivery.createRoute(userName))
                },
                onNavigateToWallet = {
                    navController.navigate(Screen.Wallet.createRoute(userName))
                },
                viewModel = viewModel
            )
        }

        // ... (The rest of your NavGraph.kt file remains unchanged)

        // 🔹 Menu
        composable(
            route = Screen.Menu.route,
            arguments = listOf(navArgument("user_name") { type = NavType.StringType })
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("user_name") ?: "Guest"
            val viewModel: MenuViewModel = viewModel()
            MenuScreen(
                userName = userName,
                onNavigateToHome = {
                    navController.navigate(Screen.CustomerHome.createRoute(userName)) {
                        popUpTo(Screen.CustomerHome.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToOrders = {
                    navController.navigate(Screen.Orders.createRoute(userName)) {
                        launchSingleTop = true
                    }
                },
                viewModel = viewModel
            )
        }

        // 🔹 Orders
        composable(
            route = Screen.Orders.route,
            arguments = listOf(navArgument("user_name") { type = NavType.StringType })
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("user_name") ?: "Guest"
            val viewModel: OrdersViewModel = viewModel()
            OrdersScreen(
                userName = userName,
                onNavigateToMenu = {
                    navController.navigate(Screen.Menu.createRoute(userName)) {
                        launchSingleTop = true
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.CustomerHome.createRoute(userName)) {
                        popUpTo(Screen.CustomerHome.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                viewModel = viewModel
            )
        }

        // 🔹 Pick Up
        composable(
            route = Screen.PickUp.route,
            arguments = listOf(navArgument("user_name") { type = NavType.StringType })
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("user_name") ?: "Guest"
            val viewModel: PickUpViewModel = viewModel()

            PickUpScreen(
                userName = userName,
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onOrderNow = {
                    navController.navigate(Screen.Orders.createRoute(userName)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // 🔹 Delivery
        composable(
            route = Screen.Delivery.route,
            arguments = listOf(navArgument("user_name") { type = NavType.StringType })
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("user_name") ?: "Guest"
            val viewModel: DeliveryViewModel = viewModel()

            DeliveryScreen(
                userName = userName,
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onOrderNow = {
                    navController.navigate(Screen.Orders.createRoute(userName)) {
                        launchSingleTop = true
                    }
                }
            )
        }
        // 🔹 Wallet
        composable(
            route = Screen.Wallet.route,
            arguments = listOf(navArgument("user_name") { type = NavType.StringType })
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("user_name") ?: "Guest"
            val viewModel: WalletViewModel = viewModel()

            WalletScreen(
                userName = userName,
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
