package com.example.colfi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.colfi.ui.theme.LightCream1
import com.example.colfi.ui.theme.colfiFont
import com.example.colfi.ui.viewmodel.ProfileViewModel
import com.example.colfi.ui.viewmodel.WalletViewModel

@Composable
fun ProfileScreen(
    userName: String,
    onNavigateToMenu: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToOrders: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
    walletViewModel: WalletViewModel = viewModel()
) {
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val walletUiState by walletViewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding() // Ensure navigation bar padding is applied
            .background(LightCream1)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Profile",
                fontFamily = colfiFont,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(32.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator()
                }
                errorMessage != null -> {
                    Text(text = errorMessage ?: "Unknown error")
                }
                user != null -> {
                    Text("Name: ${user!!.displayName}", fontFamily = colfiFont)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Email: ${user!!.email}", fontFamily = colfiFont)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Wallet: RM ${String.format("%.2f", walletUiState.balance)}", fontFamily = colfiFont)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Points: ${user!!.points}", fontFamily = colfiFont)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Vouchers: ${user!!.vouchers}", fontFamily = colfiFont)

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { viewModel.logout(onNavigateToLogin) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Logout", fontFamily = colfiFont)
                    }
                }
                else -> {
                    Text("No user data available")
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
            onProfileClick = { },
            isHomeSelected = false,
            isMenuSelected = false,
            isOrdersSelected = false,
            isProfileSelected = true
        )
    }
}
