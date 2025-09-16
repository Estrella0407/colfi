// StaffOrdersScreen.kt
package com.example.colfi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colfi.R
import com.example.colfi.ui.theme.DarkBrown1
import com.example.colfi.ui.theme.LightCream1
import com.example.colfi.ui.theme.colfiFont
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.colfi.ui.viewmodel.StaffOrdersViewModel


@Composable
fun StaffOrdersScreen(
    onNavigateToProducts: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: StaffOrdersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(LightCream1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 56.dp) // Adjust for bottom navigation height
        ) {
            OngoingOrdersHeader(selectedTab = selectedTab, onTabSelected = { viewModel.selectTab(it) })

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFFD2B48C)
                        )
                    }
                    uiState.errorMessage.isNotEmpty() -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error loading orders",
                                fontFamily = colfiFont,
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = uiState.errorMessage,
                                fontFamily = colfiFont,
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
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
            onStaffOrdersClick = { },
            onProductsClick = onNavigateToProducts,
            onProfileClick = onNavigateToProfile,
            isStaffOrdersSelected = true,
            isProductsSelected = false,
            isProfileSelected = false
        )
    }
}

@Composable
fun OngoingOrdersHeader(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    Column {
        // Title row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .background(LightCream1)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Ongoing Orders",
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

        // Tab buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5DC))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TabButton(
                text = "All",
                isSelected = selectedTab == "all",
                onClick = { onTabSelected("all") },
                modifier = Modifier.weight(1f)
            )

            TabButton(
                text = "Dine In",
                isSelected = selectedTab == "dine_in",
                onClick = { onTabSelected("dine_in") },
                modifier = Modifier.weight(1f)
            )

            TabButton(
                text = "Pick Up",
                isSelected = selectedTab == "pick_up",
                onClick = { onTabSelected("pick_up") },
                modifier = Modifier.weight(1f)
            )

            TabButton(
                text = "Delivery",
                isSelected = selectedTab == "delivery",
                onClick = { onTabSelected("delivery") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StaffBottomNavigation(
    modifier: Modifier = Modifier,
    onStaffOrdersClick: () -> Unit = {},
    onProductsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    isStaffOrdersSelected: Boolean = false,
    isProductsSelected: Boolean = false,
    isProfileSelected: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = LightCream1,
            contentColor = DarkBrown1
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomNavItem(
                iconRes = R.drawable.order_history,
                label = "Orders",
                isSelected = isStaffOrdersSelected,
                onClick = onStaffOrdersClick
            )
            BottomNavItem(
                iconRes = R.drawable.product_management,
                label = "Products",
                isSelected = isProductsSelected,
                onClick = onProductsClick
            )
            BottomNavItem(
                iconRes = R.drawable.profile_icon,
                label = "Me",
                isSelected = isProfileSelected,
                onClick = onProfileClick
            )
        }
    }
}