// CustomerProfileScreen.kt
package com.example.colfi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.colfi.ui.theme.LightBrown2
import com.example.colfi.ui.theme.LightCream1
import com.example.colfi.ui.theme.LightCream2
import com.example.colfi.ui.theme.colfiFont
import com.example.colfi.ui.viewmodel.CustomerProfileViewModel
import com.example.colfi.ui.viewmodel.WalletViewModel

@Composable
fun CustomerProfileScreen(
    userName: String,
    onNavigateToMenu: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToOrders: () -> Unit,
    viewModel: CustomerProfileViewModel = viewModel(),
    walletViewModel: WalletViewModel = viewModel()
) {
    val user by viewModel.customer.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val walletUiState by walletViewModel.uiState.collectAsState()
    val isLoggedOut by viewModel.isLoggedOut.collectAsState()

    // Handle logout navigation
    LaunchedEffect(isLoggedOut) {
        if (isLoggedOut) {
            onNavigateToLogin()
            viewModel.onLogoutHandled()
        }
    }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(LightCream1)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Profile — COLFi —",
                fontFamily = colfiFont,
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LightBrown2)
                    }
                }
                errorMessage != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error loading profile", fontFamily = colfiFont, fontSize = 16.sp, color = Color.Red)
                        Text(errorMessage ?: "Unknown error", fontFamily = colfiFont, fontSize = 14.sp)
                        Button(
                            onClick = { viewModel.loadCustomer() },
                            colors = ButtonDefaults.buttonColors(containerColor = LightBrown2),
                            modifier = Modifier.padding(top = 16.dp)
                        ) { Text("Retry", fontFamily = colfiFont) }
                    }
                }
                user != null -> {
                    // Profile image placeholder
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color(0xFFD4B8A8), shape = androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user!!.displayName?.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                            fontFamily = colfiFont,
                            fontSize = 48.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Info cards
                    ProfileInfoCard("Name", user!!.displayName ?: "Not provided", isEditable = true)
                    ProfileInfoCard("Email", user!!.email ?: "Not provided", isEditable = true)
                    ProfileInfoCard("Wallet", "RM ${String.format("%.2f", user!!.walletBalance)}")
                    ProfileInfoCard("Points", user!!.points.toString())
                    ProfileInfoCard("Vouchers", user!!.vouchers.toString())

                    Spacer(modifier = Modifier.weight(1f))

                    // Logout button
                    Button(
                        onClick = { viewModel.logout() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Log Out", fontFamily = colfiFont, fontSize = 16.sp, color = Color.White)
                    }
                }
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No user data available", fontFamily = colfiFont, fontSize = 16.sp, color = Color.Gray)
                        Button(
                            onClick = { viewModel.loadCustomer() },
                            colors = ButtonDefaults.buttonColors(containerColor = LightBrown2),
                            modifier = Modifier.padding(top = 16.dp)
                        ) { Text("Load Profile", fontFamily = colfiFont) }
                    }
                }
            }
        }

        // Bottom Navigation Bar
        BottomNavigation(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            onHomeClick = onNavigateToHome,
            onMenuClick = onNavigateToMenu,
            onOrdersClick = onNavigateToOrders,
            onCustomerProfileClick = { },
            isHomeSelected = false,
            isMenuSelected = false,
            isOrdersSelected = false,
            isCustomerProfileSelected = true
        )
    }
}
