// CustomerProfileScreen.kt
package com.example.colfi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
                .padding(bottom = 80.dp)
        ) {
            // Header
            ProfileHeader()

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LightBrown2)
                    }
                }
                errorMessage != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp) // Added padding
                    ) {
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

                    // Info cards with horizontal padding
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp), // Added horizontal padding here
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ProfileInfoCard("Name", user!!.displayName ?: "Not provided")
                        ProfileInfoCard("Email", user!!.email ?: "Not provided")
                        ProfileInfoCard("Wallet", "RM ${String.format("%.2f", user!!.walletBalance)}")
                        ProfileInfoCard("Points", user!!.points.toString())
                        ProfileInfoCard("Vouchers", user!!.vouchers.toString())

                        // Logout button
                        Button(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = LightBrown2),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Log Out", fontFamily = colfiFont, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp) // Added padding
                    ) {
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

@Composable
fun ProfileInfoCard(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp), // Remove horizontal padding since parent already has it
        colors = CardDefaults.cardColors(containerColor = LightCream2),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontFamily = colfiFont,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Text(
                text = value,
                fontFamily = colfiFont,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun ProfileHeader() {
    Column {
        // Title row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .background(LightCream1)
                .padding(horizontal = 24.dp, vertical = 16.dp) // Consistent horizontal padding
        ) {
            Text(
                text = "Profile",
                fontFamily = colfiFont,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "— COLFi —",
                fontFamily = colfiFont,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}