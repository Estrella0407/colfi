// Screen.kt
package com.example.colfi.navigation

sealed class Screen(val route: String) {
    object Loading : Screen("loading")
    object Login : Screen("login")
    object RoleSelection : Screen("role_selection/{is_for_registration}") {
        fun createRoute(isForRegistration: Boolean): String = "role_selection/$isForRegistration"
    }
    object Register : Screen("register/{role}") {
        fun createRoute(role: String): String = "register/$role"
    }
    object CustomerHome : Screen("customer_home/{user_name}") {
        fun createRoute(userName: String) = "customer_home/$userName"
    }
    object Menu : Screen("menu/{user_name}") {
        fun createRoute(userName: String) = "menu/$userName"
    }
    object Orders : Screen("orders/{user_name}") {
        fun createRoute(userName: String) = "orders/$userName"
    }
    object CustomerProfile : Screen("customer_profile/{user_name}") {
        fun createRoute(userName: String) = "customer_profile/$userName"
    }
    object Cart : Screen("cart/{user_name}") {
        fun createRoute(userName: String) = "cart/$userName"
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
    object StaffOrders : Screen("staff_orders/{user_name}") {
        fun createRoute(userName: String) = "staff_orders/$userName"
    }
    object Products : Screen("products")
    object StaffProfile : Screen("staff_profile/{user_name}") {
        fun createRoute(userName: String) = "staff_profile/$userName"
    }
}