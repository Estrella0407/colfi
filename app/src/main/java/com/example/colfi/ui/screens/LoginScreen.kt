// LoginScreen.kt
package com.example.colfi.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.colfi.ui.theme.*
import com.example.colfi.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onNavigateToHome: (String) -> Unit,
    onNavigateAsGuest: () -> Unit, // <<< 1. ADD THIS NEW PARAMETER for guest navigation
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // --- 2. ADD THIS LaunchedEffect TO HANDLE NAVIGATION ---
    // This block observes the login state and navigates automatically on success.
    LaunchedEffect(uiState.isLoginSuccessful, uiState.loggedInUserName) {
        // When isLoginSuccessful becomes true and we have a username...
        if (uiState.isLoginSuccessful && uiState.loggedInUserName != null) {
            // ...navigate to the home screen...
            onNavigateToHome(uiState.loggedInUserName!!)
            // ...and immediately tell the ViewModel the navigation has been handled.
            viewModel.onLoginHandled()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "COLFi",
            fontFamily = colfiFont,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Login",
            fontFamily = colfiFont,
            fontSize = 16.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = uiState.username,
            onValueChange = viewModel::updateUsername,
            label = { Text("Username", color = DarkBrown1) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LightBrown1,
                unfocusedBorderColor = DarkBrown1
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::updatePassword,
            label = { Text("Password", color = DarkBrown1) },
            // ... other properties are fine ...
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (uiState.passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = viewModel::togglePasswordVisibility) {
                    Icon(imageVector = image, contentDescription = if (uiState.passwordVisible) "Hide password" else "Show password")
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LightBrown1,
                unfocusedBorderColor = DarkBrown1
            )
        )

        if (uiState.errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            // --- 3. UPDATE THE onClick LAMBDA ---
            onClick = { viewModel.login() }, // Simply call the login function
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = LightCream3)
            } else {
                Text(text = "Login", fontFamily = colfiFont)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End // Align to the right
        ) {
            Text(
                text = "Continue as Guest",
                color = DarkBrown1,
                fontFamily = colfiFont,
                fontSize = 14.sp,
                textDecoration = TextDecoration.Underline, // underline
                modifier = Modifier.clickable {
                    viewModel.loginAsGuest()
                }
            )
        }

        // Register Button
        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register",
                color = DarkBrown1,
                fontFamily = colfiFont,
                fontSize = 14.sp
                )
        }

        // Display error message if it exists
        if (uiState.errorMessage.isNotEmpty()) {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

    }
}