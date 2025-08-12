// NavGraph.kt
package com.example.colfi.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.colfi.ui.screens.*
import com.example.colfi.ui.viewmodel.*

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Loading.route
    ) {
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

        composable(Screen.Login.route) {
            val viewModel: LoginViewModel = viewModel()
            LoginScreen(
                onNavigateToHome = { userName ->
                    navController.navigate(Screen.CustomerHome.createRoute(userName)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        composable(Screen.CustomerHome.route) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("user_name") ?: "Guest"
            val viewModel: HomeViewModel = viewModel()
            CustomerHomeScreen(
                userName = userName,
                onNavigateToMenu = {
                    navController.navigate(Screen.Menu.route)
                },
                onNavigateToOrders = {
                    navController.navigate(Screen.Orders.route)
                },
                viewModel = viewModel
            )
        }

        composable(Screen.Menu.route) {
            val viewModel: MenuViewModel = viewModel()
            MenuScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }

        composable(Screen.Orders.route) {
            val viewModel: OrdersViewModel = viewModel()
            OrdersScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }
    }
}