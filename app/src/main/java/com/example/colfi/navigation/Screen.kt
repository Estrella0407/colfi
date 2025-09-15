// Screen.kt
package com.example.colfi.navigation

sealed class Screen(val route: String) {
    object Loading : Screen("loading")
    object Register : Screen("register/{user_name}"){
        fun createRoute(userName: String) = "register/$userName"
    }
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
    object Profile : Screen("profile/{user_name}") {
        fun createRoute(userName: String) = "profile/$userName"
    }

    object Cart : Screen("cart/{user_name}") {
        fun createRoute(userName: String) = "cart/$userName"
    }
    object ItemDetail : Screen("itemDetail/{item_id}") {
        fun createRoute(itemId: String) = "itemDetail/$itemId"
    }
    object DineIn : Screen("dinein/{user_name}") {
        fun createRoute(userName: String) = "dinein/$userName"
    }
    object PickUp : Screen("pickup/{user_name}") {
        fun createRoute(userName: String) = "pickup/$userName"
    }
    object Delivery : Screen("delivery/{user_name}") {
        fun createRoute(userName: String) = "delivery/$userName"
    }
    object Wallet : Screen("wallet/{user_name}") {
        fun createRoute(userName: String) = "wallet/$userName"
    }
    object Checkout : Screen("checkout/{user_name}") {
        fun createRoute(userName: String) = "checkout/$userName"
    }
}