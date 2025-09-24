package com.example.colfi.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White // Already here, good
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.colfi.ui.viewmodel.WalletViewModel
import com.example.colfi.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    userName: String,
    navController: NavHostController,
    viewModel: WalletViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Initialize the wallet when the screen is first displayed
    LaunchedEffect(Unit) {
        Log.d("WalletScreen", "Initializing wallet for user: $userName")
        viewModel.initialize() // Call without parameters
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Wallet", fontFamily = colfiFont) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightCream1,
                    titleContentColor = DarkBrown1,
                    navigationIconContentColor = DarkBrown1
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Show loading indicator
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(vertical = 20.dp),
                    color = DarkBrown1
                )
            }

            Text(
                "Hello, $userName",
                fontFamily = colfiFont,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "RM %.2f".format(uiState.balance),
                fontFamily = colfiFont,
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                color = DarkBrown1
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Only show wallet features if user is logged in and wallet is loaded
            if (!uiState.isLoading && uiState.message?.contains("Please log in") != true) {
                // Your wallet content here (payment methods, top-up buttons, etc.)
                Text(
                    "Choose Payment Method",
                    fontFamily = colfiFont,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = true,
                        onClick = { },
                        label = { Text("TNG") }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Select Top-Up Amount",
                    fontFamily = colfiFont,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TopUpButton(
                        amount = 20.0,
                        onTopUp = { amountValue -> viewModel.topUp(amountValue) },
                        isLoading = uiState.isLoading
                    )
                    TopUpButton(
                        amount = 40.0,
                        onTopUp = { amountValue -> viewModel.topUp(amountValue) },
                        isLoading = uiState.isLoading
                    )
                    TopUpButton(
                        amount = 60.0,
                        onTopUp = { amountValue -> viewModel.topUp(amountValue) },
                        isLoading = uiState.isLoading
                    )
                }
            }

            // Show error/success messages
            uiState.message?.let { msg ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = msg,
                    fontFamily = colfiFont,
                    color = if (msg.contains("successful", ignoreCase = true)) {
                        Color(0xFF006400)
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                if (!msg.contains("Please log in")) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { viewModel.clearMessage() }) {
                        Text("OK", color = DarkBrown1, fontFamily = colfiFont)
                    }
                }
            }
        }
    }
}


@Composable
fun TopUpButton(amount: Double, onTopUp: (Double) -> Unit, isLoading: Boolean) {
    Button(
        onClick = { onTopUp(amount) },
        enabled = !isLoading, // Button is disabled if any wallet operation is loading
        colors = ButtonDefaults.buttonColors(
            containerColor = DarkBrown1,
            contentColor = White,
            disabledContainerColor = DarkBrown1.copy(alpha = 0.5f) // Visual feedback when disabled
        ),
        modifier = Modifier.widthIn(min = 90.dp, max = 110.dp), // Constrain size
        shape = RoundedCornerShape(8.dp) // Slightly rounded corners
    ) {
        Text(
            text = "RM ${amount.toInt()}",
            fontFamily = colfiFont
        )
    }
}

