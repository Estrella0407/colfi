// NavGraph.kt
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
        composable(Screen.Loading.route) {
            val viewModel: LoadingViewModel = viewModel()
            LoadingScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Loading.route) { inclusive = true }
                    }
                },
                onNavigateToHome = { userName ->
                    navController.navigate(Screen.CustomerHome.createRoute(userName)) {
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
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.createRoute(userName)) {
                        launchSingleTop = true
                    }
                },
                viewModel = viewModel
            )
        }

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
                        popUpTo(Screen.Menu.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToOrders = {
                    navController.navigate(Screen.Orders.createRoute(userName)) {
                        launchSingleTop = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.createRoute(userName)) {
                        launchSingleTop = true
                    }
                },
                viewModel = viewModel
            )
        }

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
                        popUpTo(Screen.Orders.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.createRoute(userName)) {
                        launchSingleTop = true
                    }
                },
                viewModel = viewModel
            )
        }

        composable(
            route = Screen.Profile.route,
            arguments = listOf(navArgument("user_name") { type = NavType.StringType })
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("user_name") ?: "Guest"
            val viewModel: ProfileViewModel = viewModel()
            ProfileScreen(
                userName = userName,
                onNavigateToHome = {
                    navController.navigate(Screen.CustomerHome.createRoute(userName)) {
                        popUpTo(Screen.Orders.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
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
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.CustomerHome.route) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }
    }
}