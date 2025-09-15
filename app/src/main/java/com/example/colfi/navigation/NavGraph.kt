// NavGraph.kt
package com.example.colfi.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.colfi.ColfiApplication
import com.example.colfi.data.repository.CartRepository
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
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                viewModel = viewModel
            )
        }

        composable(Screen.SignUp.route) {
            val viewModel: SignUpViewModel = viewModel()
            SignUpScreen(
                onNavigateToHome = { userName ->
                    navController.navigate(Screen.CustomerHome.createRoute(userName)) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        launchSingleTop = true
                        restoreState = true
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

        composable(Screen.Register.route) {
            RegisterScreen(
                // This allows the user to go back to the login screen after registering or by pressing a back button
                onNavigateBackToLogin = {
                    navController.popBackStack()
                }
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
                        restoreState = true
                    }
                },
                onNavigateToOrders = {
                    navController.navigate(Screen.Orders.createRoute(userName)) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.createRoute(userName)) {
                        launchSingleTop = true
                        restoreState = true
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

        composable(
            route = Screen.Menu.route,
            arguments = listOf(navArgument("user_name") { type = NavType.StringType })
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("user_name") ?: "Guest"
            val menuViewModel: MenuViewModel = viewModel()

            val context = LocalContext.current
            val application = context.applicationContext as ColfiApplication
            val cartRepositoryFromApplication: CartRepository = application.cartRepository

            val cartViewModel: CartViewModel = viewModel(
                factory = CartViewModelFactory(cartRepositoryFromApplication)
            )

            MenuScreen(
                userName = userName,
                menuViewModel = menuViewModel,
                cartViewModel = cartViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.CustomerHome.createRoute(userName)) {
                        popUpTo(Screen.CustomerHome.route) { inclusive = false }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToOrders = {
                    navController.navigate(Screen.Orders.createRoute(userName)) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.createRoute(userName)) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToCart = {
                    navController.navigate(Screen.Cart.createRoute(userName)) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                cartRespository = cartRepositoryFromApplication
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
                        restoreState = true
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.CustomerHome.createRoute(userName)) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.createRoute(userName)) {
                        launchSingleTop = true
                        restoreState = true
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
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToMenu = {
                    navController.navigate(Screen.Menu.createRoute(userName)) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToOrders = {
                    navController.navigate(Screen.Orders.createRoute(userName)) {
                        launchSingleTop = true
                        restoreState = true
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

        composable(
            route = Screen.Cart.route,
            arguments = listOf(navArgument("user_name") { type = NavType.StringType })
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("user_name") ?: "Guest"
            val cartViewModel: CartViewModel = viewModel()
            CartScreen(
                cartViewModel = cartViewModel,
                onNavigateBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.CustomerHome.createRoute(userName)) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onProceedToCheckout = {
                    // Navigate to checkout screen when implemented
                    // navController.navigate(Screen.Checkout.createRoute(userName))
                }
            )
        }

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


