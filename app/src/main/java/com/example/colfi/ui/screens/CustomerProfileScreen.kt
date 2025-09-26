// CustomerProfileScreen.kt
package com.example.colfi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.colfi.R
import com.example.colfi.data.model.Customer
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

    // Get screen configuration for responsive layout
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp >= configuration.screenHeightDp

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
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFD2B48C)
                )
            }
            else -> {
                if (isLandscape) {
                    // LANDSCAPE LAYOUT with sidebar
                    LandscapeProfileContent(
                        user = user,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onLogoutClick = { viewModel.logout() },
                        onRetryClick = { viewModel.loadCustomer() },
                        onNavigateToHome = onNavigateToHome,
                        onNavigateToMenu = onNavigateToMenu,
                        onNavigateToOrders = onNavigateToOrders,
                        viewModel = viewModel
                    )
                } else {
                    // PORTRAIT LAYOUT
                    PortraitProfileContent(
                        user = user,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onLogoutClick = { viewModel.logout() },
                        onRetryClick = { viewModel.loadCustomer() },
                        viewModel = viewModel
                    )
                }
            }
        }

        // Bottom Navigation Bar - Only show in portrait mode
        if (!isLandscape) {
            BottomNavigation(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(top = 8.dp),
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
}

@Composable
fun PortraitProfileContent(
    user: Customer?,
    isLoading: Boolean,
    errorMessage: String?,
    onLogoutClick: () -> Unit,
    onRetryClick: () -> Unit,
    viewModel: CustomerProfileViewModel
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Text(
                        "Error loading profile",
                        fontFamily = colfiFont,
                        fontSize = 16.sp,
                        color = Color.Red
                    )
                    Text(errorMessage ?: "Unknown error", fontFamily = colfiFont, fontSize = 14.sp)
                    Button(
                        onClick = onRetryClick,
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
                        .background(
                            Color(0xFFD4B8A8),
                            shape = androidx.compose.foundation.shape.CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.displayName?.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
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
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileInfoCard("Name", user.displayName ?: "Not provided")
                    ProfileInfoCard("Email", user.email ?: "Not provided")
                    ProfileInfoCard("Wallet", "RM ${String.format("%.2f", user.walletBalance)}")
                    ProfileInfoCard("Points", user.points.toString())
                    ProfileInfoCard("Vouchers", user.vouchers.toString())

                    // Logout button
                    Button(
                        onClick = onLogoutClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = LightBrown2),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Log Out",
                            fontFamily = colfiFont,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Text(
                        "No user data available",
                        fontFamily = colfiFont,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    Button(
                        onClick = onRetryClick,
                        colors = ButtonDefaults.buttonColors(containerColor = LightBrown2),
                        modifier = Modifier.padding(top = 16.dp)
                    ) { Text("Load Profile", fontFamily = colfiFont) }
                }
            }
        }
    }
}

@Composable
fun LandscapeProfileContent(
    user: Customer?,
    isLoading: Boolean,
    errorMessage: String?,
    onLogoutClick: () -> Unit,
    onRetryClick: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToOrders: () -> Unit,
    viewModel: CustomerProfileViewModel
) {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // Left Sidebar Navigation
        LandscapeLeftSidebar(
            onHomeClick = onNavigateToHome,
            onMenuClick = onNavigateToMenu,
            onOrdersClick = onNavigateToOrders,
            onCustomerProfileClick = { /* Already on profile */ },
            isHomeSelected = false,
            isMenuSelected = false,
            isOrdersSelected = false,
            isCustomerProfileSelected = true
        )

        // Divider
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(Color.Gray.copy(alpha = 0.3f))
        )

        // Main Content Area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header for Landscape
            LandscapeProfileHeader()

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LightBrown2)
                    }
                }
                errorMessage != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            "Error loading profile",
                            fontFamily = colfiFont,
                            fontSize = 16.sp,
                            color = Color.Red
                        )
                        Text(errorMessage ?: "Unknown error", fontFamily = colfiFont, fontSize = 14.sp)
                        Button(
                            onClick = onRetryClick,
                            colors = ButtonDefaults.buttonColors(containerColor = LightBrown2),
                            modifier = Modifier.padding(top = 16.dp)
                        ) { Text("Retry", fontFamily = colfiFont) }
                    }
                }
                user != null -> {
                    // Profile content in landscape - using side by side layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        // Left side - Profile image and basic info
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Profile image placeholder
                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .background(
                                        Color(0xFFD4B8A8),
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.displayName?.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                                    fontFamily = colfiFont,
                                    fontSize = 60.sp,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Basic info text
                            LandscapeProfileInfoText("Name", user.displayName ?: "Not provided")
                            LandscapeProfileInfoText("Email", user.email ?: "Not provided")
                        }

                        // Right side - Detailed info cards
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            LandscapeProfileInfoCard("Wallet Balance", "RM ${String.format("%.2f", user.walletBalance)}")
                            LandscapeProfileInfoCard("Points", user.points.toString())
                            LandscapeProfileInfoCard("Vouchers", user.vouchers.toString())

                            // Logout button
                            Button(
                                onClick = onLogoutClick,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = LightBrown2),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "Log Out",
                                    fontFamily = colfiFont,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            "No user data available",
                            fontFamily = colfiFont,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Button(
                            onClick = onRetryClick,
                            colors = ButtonDefaults.buttonColors(containerColor = LightBrown2),
                            modifier = Modifier.padding(top = 16.dp)
                        ) { Text("Load Profile", fontFamily = colfiFont) }
                    }
                }
            }
        }
    }
}

@Composable
fun LandscapeProfileHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Profile",
            fontFamily = colfiFont,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            text = "— COLFi —",
            fontFamily = colfiFont,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun LandscapeProfileInfoText(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = colfiFont,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Text(
            text = value,
            fontFamily = colfiFont,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun LandscapeProfileInfoCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightCream2),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontFamily = colfiFont,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Text(
                text = value,
                fontFamily = colfiFont,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun ProfileInfoCard(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp),
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
                .padding(horizontal = 24.dp, vertical = 16.dp)
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