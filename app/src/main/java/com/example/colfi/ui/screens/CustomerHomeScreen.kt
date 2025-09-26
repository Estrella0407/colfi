// CustomerHomeScreen.kt
package com.example.colfi.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.colfi.R
import com.example.colfi.data.model.Customer
import com.example.colfi.data.model.Guest
import com.example.colfi.ui.state.WalletUiState
import com.example.colfi.ui.theme.BackgroundColor
import com.example.colfi.ui.theme.DarkBrown1
import com.example.colfi.ui.theme.LightBrown2
import com.example.colfi.ui.theme.LightCream1
import com.example.colfi.ui.theme.colfiFont
import com.example.colfi.ui.viewmodel.CustomerProfileViewModel
import com.example.colfi.ui.viewmodel.HomeViewModel
import com.example.colfi.ui.viewmodel.WalletViewModel

@Composable
fun CustomerHomeScreen(
    userName: String,
    onNavigateToMenu: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToCustomerProfile: () -> Unit,
    onNavigateToDineIn: () -> Unit,
    onNavigateToPickUp: () -> Unit,
    onNavigateToDelivery: () -> Unit,
    onNavigateToWallet: (String) -> Unit,
    onNavigateToLogin: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Get screen configuration for responsive layout
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp >= configuration.screenHeightDp

    // Added WalletViewModel to observe live balance
    val walletViewModel: WalletViewModel = viewModel()
    val walletState by walletViewModel.uiState.collectAsState()

    // Initialize wallet listener
    LaunchedEffect(Unit) {
        walletViewModel.initialize()
    }

    // Handle navigation to login when authentication fails
    LaunchedEffect(uiState.shouldNavigateToLogin) {
        if (uiState.shouldNavigateToLogin) {
            onNavigateToLogin()
            viewModel.onNavigateToLoginHandled()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(Color.White)
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFD2B48C)
                )
            }

            uiState.errorMessage != null -> {
                ErrorState(
                    errorMessage = uiState.errorMessage!!,
                    onRetry = { viewModel.refreshUserData() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                if (isLandscape) {
                    // LANDSCAPE LAYOUT
                    LandscapeHomeContent(
                        uiState = uiState,
                        homeViewModel = viewModel,
                        walletState = walletState,
                        userName = userName,
                        onNavigateToMenu = onNavigateToMenu,
                        onNavigateToOrders = onNavigateToOrders,
                        onNavigateToCustomerProfile = onNavigateToCustomerProfile,
                        onNavigateToDineIn = onNavigateToDineIn,
                        onNavigateToPickUp = onNavigateToPickUp,
                        onNavigateToDelivery = onNavigateToDelivery,
                        onNavigateToWallet = onNavigateToWallet,
                        onNavigateToLogin = onNavigateToLogin,
                    )
                } else {
                    // PORTRAIT LAYOUT
                    PortraitHomeContent(
                        uiState = uiState,
                        homeViewModel = viewModel,
                        walletState = walletState,
                        scrollState = scrollState,
                        userName = userName,
                        onNavigateToMenu = onNavigateToMenu,
                        onNavigateToOrders = onNavigateToOrders,
                        onNavigateToCustomerProfile = onNavigateToCustomerProfile,
                        onNavigateToDineIn = onNavigateToDineIn,
                        onNavigateToPickUp = onNavigateToPickUp,
                        onNavigateToDelivery = onNavigateToDelivery,
                        onNavigateToWallet = onNavigateToWallet,
                        onNavigateToLogin = onNavigateToLogin,
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
                onHomeClick = { /* Already on Home */ },
                onMenuClick = onNavigateToMenu,
                onOrdersClick = onNavigateToOrders,
                onCustomerProfileClick = onNavigateToCustomerProfile,
                isHomeSelected = true,
                isMenuSelected = false,
                isOrdersSelected = false,
                isCustomerProfileSelected = false
            )
        }
    }
}

@Composable
fun PortraitHomeContent(
    uiState: com.example.colfi.ui.state.HomeUiState,
    homeViewModel: HomeViewModel,
    walletState: WalletUiState,
    scrollState: ScrollState,
    userName: String,
    onNavigateToMenu: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToCustomerProfile: () -> Unit,
    onNavigateToDineIn: () -> Unit,
    onNavigateToPickUp: () -> Unit,
    onNavigateToDelivery: () -> Unit,
    onNavigateToWallet: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val navBarHeight = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val bottomNavHeight = 64.dp
    val profileViewModel: CustomerProfileViewModel = viewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .padding(bottom = navBarHeight + bottomNavHeight),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ColfiHeader(randomQuote = uiState.randomQuote)
        Spacer(modifier = Modifier.height(32.dp))

        OrderOptions(
            onDineInClick = onNavigateToDineIn,
            onPickUpClick = onNavigateToPickUp,
            onDeliveryClick = onNavigateToDelivery
        )
        Spacer(modifier = Modifier.height(24.dp))

        when {
            uiState.user != null && uiState.user is Customer -> {
                UserInfoSection(
                    user = uiState.user as Customer,
                    walletBalance = walletState.balance,
                    onWalletClick = { onNavigateToWallet(userName) }
                )
            }
            uiState.guest != null -> {
                GuestInfoSection(
                    guest = uiState.guest!!,
                    onLogoutClick = onNavigateToLogin
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        CafeInfoSection()
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun LandscapeHomeContent(
    uiState: com.example.colfi.ui.state.HomeUiState,
    homeViewModel: HomeViewModel,
    walletState: WalletUiState,
    userName: String,
    onNavigateToMenu: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToCustomerProfile: () -> Unit,
    onNavigateToDineIn: () -> Unit,
    onNavigateToPickUp: () -> Unit,
    onNavigateToDelivery: () -> Unit,
    onNavigateToWallet: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Left Sidebar Navigation (fixed position)
        LandscapeLeftSidebar(
            onHomeClick = { /* Already on home */ },
            onMenuClick = onNavigateToMenu,
            onOrdersClick = onNavigateToOrders,
            onCustomerProfileClick = onNavigateToCustomerProfile,
            isHomeSelected = true,
            isMenuSelected = false,
            isOrdersSelected = false,
            isCustomerProfileSelected = false
        )

        // Divider
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(Color.Gray.copy(alpha = 0.3f))
        )

        // Main Content (scrollable)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            // UPPER SECTION: Barista, Quote, and Order Options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    // Header with Barista and Quote
                    LandscapeHomeHeader(randomQuote = uiState.randomQuote)
                }

                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    // Order Options
                    OrderOptions(
                        onDineInClick = onNavigateToDineIn,
                        onPickUpClick = onNavigateToPickUp,
                        onDeliveryClick = onNavigateToDelivery,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // LOWER SECTION: User Info and Store Info side by side
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left Side: User Info (Wallet, Vouchers, Points)
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    when {
                        uiState.user != null && uiState.user is Customer -> {
                            LandscapeUserInfoSection(
                                user = uiState.user as Customer,
                                walletBalance = walletState.balance,
                                onWalletClick = { onNavigateToWallet(userName) }
                            )
                        }
                        uiState.guest != null -> {
                            LandscapeGuestInfoSection(
                                guest = uiState.guest!!,
                                onLogoutClick = onNavigateToLogin
                            )
                        }
                    }
                }

                // Right Side: Store Info (Address, Time)
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    LandscapeCafeInfoSection()
                }
            }

            // Add some bottom padding for better scrolling
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun LandscapeHomeHeader(randomQuote: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Barista Image on the left
        Box(
            modifier = Modifier
                .size(120.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Icon(
                painter = painterResource(id = R.drawable.barista),
                contentDescription = "COLFi Barista Character",
                modifier = Modifier.fillMaxSize(),
                tint = Color.Unspecified
            )
        }

        // Random quote in the center
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = randomQuote,
                fontFamily = colfiFont,
                fontSize = 16.sp,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun LandscapeUserInfoSection(
    user: Customer,
    walletBalance: Double,
    modifier: Modifier = Modifier,
    onWalletClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Welcome message and COLFI title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Welcome, ${user.displayName}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = colfiFont
                )

                Text(
                    text = "COLFI",
                    fontFamily = colfiFont,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wallet, Points, Vouchers in a column (vertically stacked)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Wallet
                LandscapeInfoItem(
                    title = "Wallet(RM)",
                    value = "RM %.2f".format(walletBalance),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onWalletClick() }
                )

                // Points
                LandscapeInfoItem(
                    title = "Points",
                    value = user.points.toString(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Voucher
                LandscapeInfoItem(
                    title = "Voucher",
                    value = user.vouchers.toString(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun LandscapeInfoItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = LightBrown2),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontFamily = colfiFont,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Gray
            )

            Text(
                text = value,
                fontFamily = colfiFont,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBrown1
            )
        }
    }
}

@Composable
fun LandscapeCafeInfoSection(modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Colfi Cafe",
                fontFamily = colfiFont,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Store Information in column layout
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Main Products
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Main Products:",
                        fontFamily = colfiFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = "Coffee | Tea",
                        fontFamily = colfiFont,
                        fontSize = 14.sp,
                        color = DarkBrown1
                    )
                }

                // Operating Hours
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Operating Hours:",
                        fontFamily = colfiFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = "07:00AM - 4:00PM (Mon - Fri)",
                        fontFamily = colfiFont,
                        fontSize = 14.sp,
                        color = DarkBrown1
                    )
                }

                // Address
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Address:",
                        fontFamily = colfiFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = "G-07, Ulsma Neo Asia, Jalan Raja Chulan,\nBukit Ceylon, 50200 Kuala Lumpur,\nUjilayah Persekutuan Kuala Lumpur",
                        fontFamily = colfiFont,
                        fontSize = 12.sp,
                        color = DarkBrown1,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LandscapeGuestInfoSection(
    guest: Guest,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE4D1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Browsing as Guest",
                fontFamily = colfiFont,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sign up to earn points and access your wallet!",
                fontFamily = colfiFont,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onLogoutClick,
                colors = ButtonDefaults.buttonColors(containerColor = DarkBrown1),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Sign In / Sign Up",
                    color = Color.White,
                    fontFamily = colfiFont,
                    fontSize = 14.sp
                )
            }
        }
    }
}


@Composable
fun LandscapeLeftSidebar(
    onHomeClick: () -> Unit,
    onMenuClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onCustomerProfileClick: () -> Unit,
    isHomeSelected: Boolean,
    isMenuSelected: Boolean,
    isOrdersSelected: Boolean,
    isCustomerProfileSelected: Boolean
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .fillMaxHeight()
            .background(Color.White)
            .padding(vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Home (active)
        SidebarNavItem(
            iconRes = R.drawable.homepage_icon,
            label = "Home",
            isSelected = isHomeSelected,
            onClick = onHomeClick
        )

        // Menu
        SidebarNavItem(
            iconRes = R.drawable.menu,
            label = "Menu",
            isSelected = isMenuSelected,
            onClick = onMenuClick
        )

        // Orders
        SidebarNavItem(
            iconRes = R.drawable.order_history,
            label = "Orders",
            isSelected = isOrdersSelected,
            onClick = onOrdersClick
        )

        // Profile
        SidebarNavItem(
            iconRes = R.drawable.profile_icon,
            label = "Me",
            isSelected = isCustomerProfileSelected,
            onClick = onCustomerProfileClick
        )
    }
}

@Composable
fun SidebarNavItem(
    iconRes: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(32.dp),
            tint = if (isSelected) Color(0xFFD2B48C) else Color.Gray
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontFamily = colfiFont,
            fontSize = 12.sp,
            color = if (isSelected) Color(0xFFD2B48C) else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ErrorState(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.barista),
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = errorMessage,
            textAlign = TextAlign.Center,
            color = Color.Gray,
            fontFamily = colfiFont
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = DarkBrown1)
        ) {
            Text("Retry", color = Color.White)
        }
    }
}

@Composable
fun GuestInfoSection(
    guest: Guest,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Welcome, ${guest.displayName}! 👋",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "COLFi",
                fontFamily = colfiFont,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE4D1))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Browsing as Guest",
                    fontFamily = colfiFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sign up to earn points and access your wallet!",
                    fontFamily = colfiFont,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onLogoutClick,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBrown1),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Sign In / Sign Up",
                        color = Color.White,
                        fontFamily = colfiFont,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ColfiHeader(randomQuote: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.barista),
                contentDescription = "COLFi Barista Character",
                modifier = Modifier.fillMaxSize(),
                tint = Color.Unspecified
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "— COLFi —",
                fontFamily = colfiFont,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = randomQuote,
                fontFamily = colfiFont,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun OrderOptions(
    onDineInClick: () -> Unit,
    onPickUpClick: () -> Unit,
    onDeliveryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        OrderOptionCard(R.drawable.dine_in, "Dine In", onClick = onDineInClick)
        OrderOptionCard(R.drawable.pick_up, "Pick Up", onClick = onPickUpClick)
        OrderOptionCard(R.drawable.delivery, "Delivery", onClick = onDeliveryClick)
    }
}

@Composable
fun OrderOptionCard(
    iconRes: Int,
    englishText: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = LightCream1,
            contentColor = DarkBrown1
        ),
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = englishText,
                modifier = Modifier.size(48.dp),
                tint = Color.Unspecified
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = englishText,
                fontFamily = colfiFont,
            )
        }
    }
}

@Composable
fun UserInfoSection(
    user: Customer,
    walletBalance: Double,
    modifier: Modifier = Modifier,
    onWalletClick: () -> Unit
) {
    val profileViewModel: CustomerProfileViewModel = viewModel()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Welcome message with user data
            Text(
                text = "Welcome, ${user.displayName} 👋",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "COLFi",
                fontFamily = colfiFont,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        // Wallet and points info from user model
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoCard(
                title = "RM %.2f".format(walletBalance),
                subtitle = "Wallet (RM)",
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onWalletClick()
                        profileViewModel.refreshProfile()}
            )
            Spacer(modifier = Modifier.width(16.dp))
            InfoCard(
                title = user.points.toString(),
                subtitle = "Points",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            InfoCard(
                title = user.vouchers.toString(),
                subtitle = "Vouchers",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun InfoCard(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE4D1))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontFamily = colfiFont, fontSize = 18.sp, fontWeight = FontWeight.Normal)
            Text(text = subtitle, fontFamily = colfiFont, fontSize = 14.sp)
        }
    }
}

@Composable
fun CafeInfoSection(modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = LightCream1,
            contentColor = DarkBrown1
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "COLFi Cafe",
                fontFamily = colfiFont,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CafeInfoParagraphs(
                "Main Products:",
                "Coffee | Tea"
            )

            Spacer(modifier = Modifier.height(12.dp))

            CafeInfoParagraphs(
                "Operating Hours:",
                "07:00AM - 4:00PM (Mon - Fri)"
            )

            Spacer(modifier = Modifier.height(12.dp))

            CafeInfoParagraphs(
                "Address:",
                "G-07, Wisma New Asia, Jalan Raja Chulan,\nBukit Ceylon, 50200 Kuala Lumpur,\nWilayah Persekutuan Kuala Lumpur"
            )
        }
    }
}

@Composable
fun CafeInfoParagraphs(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = title,
            fontFamily = colfiFont,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontFamily = colfiFont,
            fontSize = 14.sp
        )
    }
}

@Composable
fun BottomNavigation(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onCustomerProfileClick: () -> Unit = {},
    isHomeSelected: Boolean = false,
    isOrdersSelected: Boolean = false,
    isCustomerProfileSelected: Boolean = false,
    isMenuSelected: Boolean = false
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
                iconRes = R.drawable.homepage_icon,
                label = "Home",
                isSelected = isHomeSelected,
                onClick = onHomeClick
            )
            BottomNavItem(
                iconRes = R.drawable.menu,
                label = "Menu",
                isSelected = isMenuSelected,
                onClick = onMenuClick
            )
            BottomNavItem(
                iconRes = R.drawable.order_history,
                label = "Orders",
                isSelected = isOrdersSelected,
                onClick = onOrdersClick
            )
            BottomNavItem(
                iconRes = R.drawable.profile_icon,
                label = "Me",
                isSelected = isCustomerProfileSelected,
                onClick = onCustomerProfileClick
            )
        }
    }
}

@Composable
fun BottomNavItem(
    iconRes: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = if (isSelected) DarkBrown1 else Color.Gray,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = label,
            modifier = Modifier.padding(top = 2.dp),
            color = if (isSelected) DarkBrown1 else Color.Gray
        )
    }
}