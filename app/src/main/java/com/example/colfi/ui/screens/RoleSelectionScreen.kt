// RoleSelectionScreen.kt
package com.example.colfi.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
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
import com.example.colfi.ui.theme.DarkBrown1
import com.example.colfi.ui.theme.LightCream1
import com.example.colfi.ui.theme.colfiFont
import com.example.colfi.ui.viewmodel.RoleSelectionViewModel

@Composable
fun RoleSelectionScreen(
    isForRegistration: Boolean = false, // Flag to distinguish between login and registration flow
    onRegisterWithRole: ((String) -> Unit)? = null, // Callback for registration with role
    viewModel: RoleSelectionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Show error dialog if there's an error
    if (uiState.errorMessage.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(uiState.errorMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isForRegistration) "Select your role to register" else "Which character are you?",
                fontFamily = colfiFont,
                fontSize = 24.sp,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            CharacterCard(
                iconRes = R.drawable.coffee_lover,
                role = "Customer",
                roleTitle = "I'm a coffee lover",
                description = "Get your caffeine fix while staying, get it on the go or have it delivered to your home",
                onClick = {
                    viewModel.selectRole("customer")
                    if (isForRegistration) {
                        onRegisterWithRole?.invoke("customer")
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            CharacterCard(
                iconRes = R.drawable.barista,
                role = "Barista",
                roleTitle = "I'm a coffee maker",
                description = "Use your crafty hands to brew some happiness with some touch of caffeine",
                onClick = {
                    viewModel.selectRole("staff")
                    if (isForRegistration) {
                        onRegisterWithRole?.invoke("staff")
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CharacterCard(
    iconRes: Int,
    role: String,
    roleTitle: String,
    description: String,
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
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = role,
                modifier = Modifier.size(48.dp),
                tint = Color.Unspecified
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = roleTitle,
                    fontFamily = colfiFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    fontFamily = colfiFont,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}