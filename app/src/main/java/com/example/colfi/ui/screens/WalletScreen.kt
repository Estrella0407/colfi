package com.example.colfi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.colfi.ui.viewmodel.WalletViewModel
import com.example.colfi.ui.theme.*
import com.example.colfi.ui.state.WalletUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    userName: String,
    viewModel: WalletViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Wallet", fontFamily = colfiFont) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Balance
            Text("Hello, $userName", fontFamily = colfiFont)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "RM %.2f".format(uiState.balance),
                fontFamily = colfiFont,
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.headlineMedium.fontSize
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Payment Method
            Text("Choose Payment Method", fontFamily = colfiFont, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                MethodChip("TnG", uiState.selectedMethod, onSelect = { viewModel.selectMethod(it) })
                MethodChip("Bank", uiState.selectedMethod, onSelect = { viewModel.selectMethod(it) })
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Top-up buttons
            Text("Select Top-Up Amount", fontFamily = colfiFont, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                TopUpButton(20.0) { viewModel.topUp(it) }
                TopUpButton(40.0) { viewModel.topUp(it) }
                TopUpButton(60.0) { viewModel.topUp(it) }
            }

            // Success Snackbar
            uiState.message?.let { msg ->
                Spacer(modifier = Modifier.height(16.dp))
                Snackbar(
                    action = {
                        TextButton(onClick = { viewModel.clearMessage() }) {
                            Text("OK", color = DarkBrown1)
                        }
                    }
                ) { Text(msg, fontFamily = colfiFont) }
            }
        }
    }
}

@Composable
fun MethodChip(label: String, selected: String, onSelect: (String) -> Unit) {
    FilterChip(
        selected = selected == label,
        onClick = { onSelect(label) },
        label = { Text(label, fontFamily = colfiFont) }
    )
}

@Composable
fun TopUpButton(amount: Double, onTopUp: (Double) -> Unit) {
    Button(
        onClick = { onTopUp(amount) },
        modifier = Modifier,
        colors = ButtonDefaults.buttonColors(containerColor = DarkBrown1)
    ) {
        Text("RM ${amount.toInt()}", fontFamily = colfiFont, color = White)
    }
}
