package com.example.colfi.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
// import androidx.compose.foundation.clickable // Not directly used, can be removed if not needed by other elements in this file
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Import Color
import androidx.compose.ui.graphics.Color.Companion.White // Already here, good
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.colfi.ui.viewmodel.WalletViewModel
import com.example.colfi.ui.theme.*
// Ensure WalletUiState is imported explicitly if needed, though often inferred.
// import com.example.colfi.ui.state.WalletUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    userName: String, // userName is used for viewModel.initialize() and for display
    viewModel: WalletViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load wallet on screen start using the userName passed to this screen.
    // The WalletViewModel's initialize function currently uses this userName.
    LaunchedEffect(userName) {
        Log.d(
            "WalletScreen",
            "Initializing WalletViewModel with userName for initial read: $userName"
        )
        viewModel.initialize(userName)
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
                colors = TopAppBarDefaults.topAppBarColors( // Optional: Theming for TopAppBar
                    containerColor = LightCream1,
                    titleContentColor = DarkBrown1,
                    navigationIconContentColor = DarkBrown1
                )
            )
        }
    ) { paddingValues -> // Renamed 'padding' to 'paddingValues' for clarity as per Material3 convention
        Column(
            modifier = Modifier
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(16.dp) // Apply additional screen-specific padding
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Log.d("WalletScreenDebug", "Composing WalletScreen. isLoading: ${uiState.isLoading}, selectedMethod: ${uiState.selectedMethod}, balance: ${uiState.balance}, message: ${uiState.message}")
            // Simple Loading Indicator (visible only when isLoading is true and no specific message is shown)
            if (uiState.isLoading && uiState.message == null) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(vertical = 20.dp),
                    color = DarkBrown1 // Use a theme color
                )
            }

            Text(
                "Hello, $userName", // Display the userName passed to the screen
                fontFamily = colfiFont,
                style = MaterialTheme.typography.titleMedium // Use MaterialTheme typography
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "RM %.2f".format(uiState.balance),
                fontFamily = colfiFont,
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                color = DarkBrown1 // Use a theme color
            )

            Spacer(modifier = Modifier.height(24.dp))

            Log.d("WalletScreenDebug", "Before 'Choose Payment Method' Row. isLoading: ${uiState.isLoading}")
            Text(
                "Choose Payment Method",
                fontFamily = colfiFont,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    16.dp,
                    Alignment.CenterHorizontally
                ), // Spaced by and centered
                modifier = Modifier.fillMaxWidth()
            ) {
                Log.d("WalletScreenDebug", "Composing Payment Method Row. Number of chips expected: 2")
                FilterChip(
                    selected = true,
                    onClick = { Log.d("FilterChipTest", "Chip clicked") },
                    label = { Text("Test Chip") },
                    enabled = true)
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
                horizontalArrangement = Arrangement.SpaceEvenly, // Or Arrangement.spacedBy(8.dp)
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

            // Show success/failure message from uiState
            uiState.message?.let { msg ->
                Spacer(modifier = Modifier.height(24.dp)) // Increased spacer
                Text(
                    text = msg,
                    fontFamily = colfiFont,
                    color = if (msg.contains("successful", ignoreCase = true)) {
                        Color(0xFF006400) // Darker green for success
                    } else {
                        MaterialTheme.colorScheme.error // Use theme error color for failure
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { viewModel.clearMessage() }) {
                    Text("OK", color = DarkBrown1, fontFamily = colfiFont)
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

