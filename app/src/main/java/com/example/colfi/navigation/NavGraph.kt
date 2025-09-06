package com.example.colfi.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.colfi.ui.screens.*
import com.example.colfi.ui.viewmodel.*

@Composable
fun NavGraph(navController: NavHostController) {
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

        // 🔹 Login
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
                viewModel = viewModel
            )
        }

        // 🔹 Menu
        composable(
            route = Screen.Menu.route,
            arguments = listOf(navArgument("user_name") { type = NavType.StringType })
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("user_name") ?: "Guest"
            val viewModel: MenuViewModel = viewModel()
            val cartViewModel: CartViewModel = viewModel()

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
                onNavigateToCart = {
                    navController.navigate(Screen.Cart.createRoute(userName)) {
                        launchSingleTop = true
                    }
                },
                onNavigateToItemDetail = { itemId ->
                    navController.navigate(Screen.ItemDetail.createRoute(itemId))
                },
                viewModel = viewModel,
                cartViewModel = cartViewModel
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

        // 🔹 Cart
        composable(
            route = Screen.Cart.route,
            arguments = listOf(navArgument("user_name") { type = NavType.StringType })
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("user_name") ?: "Guest"
            val cartViewModel: CartViewModel = viewModel()
            CartScreen(
                userName = userName,
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.CustomerHome.createRoute(userName)) {
                            launchSingleTop = true
                        }
                    }
                },
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
                viewModel = cartViewModel
            )
        }
    }
}
