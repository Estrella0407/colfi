// Screen.kt
package com.example.colfi.navigation

sealed class Screen(val route: String) {
    object Loading : Screen("loading")
    object Login : Screen("login")
    object CustomerHome : Screen("customer_home/{user_name}") {
        fun createRoute(userName: String) = "customer_home/$userName"
    }
    object Menu : Screen("menu")

    object Orders : Screen("orders")
}