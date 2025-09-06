// Screen.kt
package com.example.colfi.navigation

sealed class Screen(val route: String) {
    object Loading : Screen("loading")
    object Login : Screen("login")
    object CustomerHome : Screen("customer_home/{user_name}") {
        fun createRoute(userName: String) = "customer_home/$userName"
    }
    object Menu : Screen("menu/{user_name}") {
        fun createRoute(userName: String) = "menu/$userName"
    }
    object Orders : Screen("orders/{user_name}") {
        fun createRoute(userName: String) = "orders/$userName"
    }
    object Cart : Screen("cart/{user_name}") {
        fun createRoute(userName: String) = "cart/$userName"
    }
    object ItemDetail : Screen("itemDetail/{item_id}") {
        fun createRoute(itemId: String) = "itemDetail/$itemId"
    }

}