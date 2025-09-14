// CustomerHomeScreen.kt
package com.example.colfi.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.colfi.R
import com.example.colfi.data.model.User
import com.example.colfi.ui.theme.DarkBrown1
import com.example.colfi.ui.theme.LightBrown1
import com.example.colfi.ui.theme.LightCream1
import com.example.colfi.ui.theme.colfiFont
import com.example.colfi.ui.viewmodel.HomeViewModel

@Composable
fun CustomerHomeScreen(
    userName: String,
    onNavigateToMenu: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(userName) {
        viewModel.initialize(userName)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFD2B48C)
            )
        } else {
            val navBarHeight = WindowInsets.navigationBars
                .asPaddingValues()
                .calculateBottomPadding()

            val bottomNavHeight = 64.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
                    .padding(bottom = navBarHeight + bottomNavHeight), // dynamic bottom space
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ColfiHeader(randomQuote = uiState.randomQuote)
                Spacer(modifier = Modifier.height(32.dp))
                OrderOptions()
                Spacer(modifier = Modifier.height(24.dp))
                uiState.user?.let { user ->
                    UserInfoSection(user = user)
                }
                Spacer(modifier = Modifier.height(24.dp))
                CafeInfoSection()
                Spacer(modifier = Modifier.height(24.dp))
            }

            BottomNavigation(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                onHomeClick = { /* Already on Home */ },
                onMenuClick = onNavigateToMenu,
                onOrdersClick = onNavigateToOrders,
                onProfileClick = onNavigateToProfile,
                isHomeSelected = true,
                isOrdersSelected = false,
                isProfileSelected = false
            )

        }
    }
}


@Composable
fun ColfiHeader(randomQuote: String, modifier: Modifier = Modifier) {
    val colfiFontFamily = colfiFont

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
                fontFamily = colfiFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = randomQuote,
                fontFamily = colfiFontFamily,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun OrderOptions(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        OrderOptionCard(R.drawable.dine_in, "Dine In") { /* Handle dine in */ }
        OrderOptionCard(R.drawable.pick_up, "Pick Up") { /* Handle pick up */ }
        OrderOptionCard(R.drawable.delivery, "Delivery") { /* Handle delivery */ }
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
fun UserInfoSection(user: User, modifier: Modifier = Modifier) {
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
                title = String.format("%.2f", user.walletBalance),
                subtitle = "Wallet (RM)",
                modifier = Modifier.weight(1f)
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
            modifier = Modifier.fillMaxWidth().padding(16.dp),
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
    onProfileClick: () -> Unit = {},
    isHomeSelected: Boolean = false,
    isOrdersSelected: Boolean = false,
    isProfileSelected: Boolean = false,
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
                iconRes = R.drawable.homepage_icon, // Replace with actual home icon resource
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
                isSelected = isProfileSelected,
                onClick = onProfileClick
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

