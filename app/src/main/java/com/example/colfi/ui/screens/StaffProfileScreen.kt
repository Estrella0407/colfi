// StaffProfileScreen.kt
package com.example.colfi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Profile — COLFi —",
                fontFamily = colfiFont,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = LightBrown2
                        )
                    }
                }
                errorMessage != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "Error loading profile",
                            fontFamily = colfiFont,
                            fontSize = 16.sp,
                            color = Color(0xFFD32F2F)
                        )
                        Text(
                            text = errorMessage ?: "Unknown error",
                            fontFamily = colfiFont,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Button(
                            onClick = { viewModel.loadStaff() },
                            modifier = Modifier.padding(top = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LightBrown2)
                        ) {
                            Text("Retry", fontFamily = colfiFont)
                        }
                    }
                }
                staff != null -> {
                    // Profile image placeholder with user's initial
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
                            text = staff!!.displayName?.firstOrNull()?.uppercaseChar()?.toString() ?:
                            staff!!.email?.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                            fontFamily = colfiFont,
                            fontSize = 48.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))

                    // User info cards using actual data
                    ProfileInfoCard(
                        title = "Name",
                        value = staff!!.displayName ?: "Not provided"
                    )
                    ProfileInfoCard(
                        title = "Email",
                        value = staff!!.email ?: "Not provided"
                    )

                    // Staff info section using actual data if available
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
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
                                    text = staff!!.staffSince ?: "N/A",
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
                                    text = staff!!.position ?: "Staff",
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
                                    text = staff!!.specialty ?: "General",
                                    fontFamily = colfiFont,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    // Logout button
                    Button(
                        onClick = { viewModel.logout() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LightBrown2,
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Log Out",
                            fontFamily = colfiFont,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "No user data available",
                            fontFamily = colfiFont,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Button(
                            onClick = { viewModel.loadStaff() },
                            modifier = Modifier.padding(top = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LightBrown2)
                        ) {
                            Text("Load Profile", fontFamily = colfiFont)
                        }
                    }
                }
            }
        }

        // Bottom Navigation Bar
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