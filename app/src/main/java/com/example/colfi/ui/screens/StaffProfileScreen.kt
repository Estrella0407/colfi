// StaffProfileScreen.kt
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.colfi.ui.theme.DarkBrown1
import com.example.colfi.ui.theme.LightBrown2
import com.example.colfi.ui.theme.LightCream1
import com.example.colfi.ui.theme.LightCream2
import com.example.colfi.ui.theme.colfiFont
import com.example.colfi.ui.viewmodel.StaffProfileViewModel

@Composable
fun StaffProfileScreen(
    userName: String,
    onNavigateToStaffOrders: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: StaffProfileViewModel = viewModel()
) {
    val staff by viewModel.staff.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
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

    // Handle errors
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar or dialog
            viewModel.clearErrorMessage()
        }
    }

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
                    color = LightBrown2
                )
            }
            else -> {
                if (isLandscape) {
                    // LANDSCAPE LAYOUT with sidebar
                    StaffLandscapeProfileContent(
                        staff = staff,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onLogoutClick = { viewModel.logout() },
                        onRetryClick = { viewModel.loadStaff() },
                        onNavigateToStaffOrders = onNavigateToStaffOrders,
                        onNavigateToProducts = onNavigateToProducts,
                        viewModel = viewModel
                    )
                } else {
                    // PORTRAIT LAYOUT
                    StaffPortraitProfileContent(
                        staff = staff,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onLogoutClick = { viewModel.logout() },
                        onRetryClick = { viewModel.loadStaff() },
                        viewModel = viewModel
                    )
                }
            }
        }

        // Bottom Navigation Bar - Only show in portrait mode
        if (!isLandscape) {
            StaffBottomNavigation(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                onStaffOrdersClick = onNavigateToStaffOrders,
                onProductsClick = onNavigateToProducts,
                onStaffProfileClick = { },
                isStaffOrdersSelected = false,
                isProductsSelected = false,
                isStaffProfileSelected = true
            )
        }
    }
}

@Composable
fun StaffPortraitProfileContent(
    staff: com.example.colfi.data.model.Staff?,
    isLoading: Boolean,
    errorMessage: String?,
    onLogoutClick: () -> Unit,
    onRetryClick: () -> Unit,
    viewModel: StaffProfileViewModel
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        // Header
        StaffProfileHeader()

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
            staff != null -> {
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
                        text = staff.displayName?.firstOrNull()?.uppercaseChar()?.toString() ?:
                        staff.email?.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
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
                    ProfileInfoCard("Name", staff.displayName ?: "Not provided")
                    ProfileInfoCard("Email", staff.email ?: "Not provided")

                    // Staff info section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = LightCream2)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Staff Since",
                                    fontFamily = colfiFont,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = staff.staffSince ?: "N/A",
                                    fontFamily = colfiFont,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Position",
                                    fontFamily = colfiFont,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = staff.position ?: "Staff",
                                    fontFamily = colfiFont,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Specialty",
                                    fontFamily = colfiFont,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = staff.specialty ?: "General",
                                    fontFamily = colfiFont,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }

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
fun StaffLandscapeProfileContent(
    staff: com.example.colfi.data.model.Staff?,
    isLoading: Boolean,
    errorMessage: String?,
    onLogoutClick: () -> Unit,
    onRetryClick: () -> Unit,
    onNavigateToStaffOrders: () -> Unit,
    onNavigateToProducts: () -> Unit,
    viewModel: StaffProfileViewModel
) {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // Left Sidebar Navigation
        StaffLandscapeLeftSidebar(
            onStaffOrdersClick = onNavigateToStaffOrders,
            onProductsClick = onNavigateToProducts,
            onStaffProfileClick = { /* Already on profile */ },
            isStaffOrdersSelected = false,
            isProductsSelected = false,
            isStaffProfileSelected = true
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
            StaffLandscapeProfileHeader()

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
                staff != null -> {
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
                                    text = staff.displayName?.firstOrNull()?.uppercaseChar()?.toString() ?:
                                    staff.email?.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                                    fontFamily = colfiFont,
                                    fontSize = 60.sp,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Basic info text
                            StaffLandscapeProfileInfoText("Name", staff.displayName ?: "Not provided")
                            StaffLandscapeProfileInfoText("Email", staff.email ?: "Not provided")
                        }

                        // Right side - Detailed info cards
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            StaffLandscapeProfileInfoCard("Position", staff.position ?: "Staff")
                            StaffLandscapeProfileInfoCard("Specialty", staff.specialty ?: "General")
                            StaffLandscapeProfileInfoCard("Staff Since", staff.staffSince ?: "N/A")

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
fun StaffLandscapeProfileHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Staff Profile",
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
fun StaffLandscapeProfileInfoText(label: String, value: String) {
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
fun StaffLandscapeProfileInfoCard(label: String, value: String) {
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
fun StaffProfileHeader() {
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
                text = "Staff Profile",
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


@Composable
fun ProfileInfoCard(
    title: String,
    value: String,
    isEditable: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightCream2)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontFamily = colfiFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = value,
                    fontFamily = colfiFont,
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (isEditable) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}