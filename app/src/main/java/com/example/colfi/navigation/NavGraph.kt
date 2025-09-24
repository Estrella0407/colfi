// NavGraph.kt
package com.example.colfi.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
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
                onNavigateToHome = { userName, userRole ->
                    val destination = when (userRole) {
                        "customer", "guest" -> Screen.CustomerHome.createRoute(userName)
                        "staff" -> Screen.StaffOrders.createRoute(userName)
                        else -> Screen.CustomerHome.createRoute(userName)
                    }
                    
                    navController.navigate(destination) {
                        popUpTo(Screen.Loading.route) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        composable(Screen.Login.route) {
            val viewModel: LoginViewModel = viewModel()
            LoginScreen(
                onNavigateToHome = { userName, role ->
                    val destination = if (role == "customer") {
                        Screen.CustomerHome.createRoute(userName)
                    } else if (role == "staff") {
                        Screen.StaffOrders.createRoute(userName)
                    } else {
                        // Handle other roles or default case
                        Screen.CustomerHome.createRoute(userName)
                    }

                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateAsGuest = {
                    navController.navigate(Screen.CustomerHome.createRoute("Guest")) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.RoleSelection.createRoute(isForRegistration = true))
                },
                viewModel = viewModel
            )
        }

        composable(
            route = Screen.RoleSelection.route,
            arguments = listOf(navArgument("is_for_registration") { type = NavType.BoolType })
        ) { backStackEntry ->
            val isForRegistrationArg = backStackEntry.arguments?.getBoolean("is_for_registration") ?: false // Default to false or true as appropriate

            RoleSelectionScreen(
                isForRegistration = isForRegistrationArg,
                onRegisterWithRole = { selectedRole ->
                    navController.navigate(Screen.Register.createRoute(selectedRole))
                }
            )
        }

        composable(Screen.Register.route,
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "customer"
            RegisterScreen(
                role = role,
                onNavigateBackToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
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
                onNavigateToCustomerProfile = {
                    navController.navigate(Screen.CustomerProfile.createRoute(userName)) {
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
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.CustomerHome.route) { inclusive = true }
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
                onNavigateToCustomerProfile = {
                    navController.navigate(Screen.CustomerProfile.createRoute(userName)) {
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
                onNavigateToCustomerProfile = {
                    navController.navigate(Screen.CustomerProfile.createRoute(userName)) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                viewModel = viewModel
            )
        }

        composable(
            route = Screen.CustomerProfile.route,
            arguments = listOf(navArgument("user_name") { type = NavType.StringType })
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("user_name") ?: "Guest"
            val viewModel: CustomerProfileViewModel = viewModel()
            CustomerProfileScreen(
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

        composable(
            route = Screen.StaffOrders.route,
            arguments = listOf(navArgument("user_name") { type = NavType.StringType })
            ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("user_name") ?: "UnknownStaff"
            val viewModel: StaffOrdersViewModel = viewModel()
            StaffOrdersScreen(
                userName = userName,
                viewModel = viewModel,
                onNavigateToProducts = {
                    navController.navigate(Screen.Products.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToStaffProfile = {
                    navController.navigate(Screen.StaffProfile.createRoute(userName)) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }

        composable(
            route = Screen.Products.route
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("user_name") ?: "Guest"
            val viewModel: ProductsViewModel = viewModel()
            ProductsScreen(
                viewModel = viewModel,
                onNavigateToStaffOrders = {
                    navController.navigate(Screen.StaffOrders.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToStaffProfile = {
                    navController.navigate(Screen.StaffProfile.createRoute(userName)) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(
            route = Screen.StaffProfile.route,
            arguments = listOf(navArgument("user_name") { type = NavType.StringType })
            ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("user_name") ?: "Guest"
            val viewModel: StaffProfileViewModel = viewModel()
            StaffProfileScreen(
                userName = userName,
                viewModel = viewModel,
                onNavigateToStaffOrders = {
                    navController.navigate(Screen.StaffOrders.createRoute(userName)) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProducts = {
                    navController.navigate(Screen.Products.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.StaffOrders.route) { inclusive = true }
                    }
                }
            )
        }
    }
}


