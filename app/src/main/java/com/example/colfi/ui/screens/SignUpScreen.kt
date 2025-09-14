// SignUpScreen.kt
package com.example.colfi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.colfi.ui.theme.*
import com.example.colfi.ui.viewmodel.SignUpViewModel

@Composable
fun SignUpScreen(
    onNavigateToHome: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SignUpViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
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
            text = "Create Account",
            fontFamily = colfiFont,
            fontSize = 16.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Email Field
        OutlinedTextField(
            value = uiState.email,
            onValueChange = viewModel::updateEmail,
            label = { Text("Email", color = DarkBrown1) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LightBrown1,
                unfocusedBorderColor = DarkBrown1
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Username Field
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

        // Display Name Field
        OutlinedTextField(
            value = uiState.displayName,
            onValueChange = viewModel::updateDisplayName,
            label = { Text("Display Name", color = DarkBrown1) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LightBrown1,
                unfocusedBorderColor = DarkBrown1
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::updatePassword,
            label = { Text("Password", color = DarkBrown1) },
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
        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password Field
        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = viewModel::updateConfirmPassword,
            label = { Text("Confirm Password", color = DarkBrown1) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (uiState.confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (uiState.confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = viewModel::toggleConfirmPasswordVisibility) {
                    Icon(imageVector = image, contentDescription = if (uiState.confirmPasswordVisible) "Hide password" else "Show password")
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LightBrown1,
                unfocusedBorderColor = DarkBrown1
            )
        )

        if (uiState.errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.signUp(onNavigateToHome) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading &&
                    uiState.email.isNotEmpty() &&
                    uiState.password.isNotEmpty() &&
                    uiState.confirmPassword.isNotEmpty() &&
                    uiState.username.isNotEmpty() &&
                    uiState.displayName.isNotEmpty()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = LightCream3)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = if (uiState.isLoading) "Creating Account..." else "Sign Up", fontFamily = colfiFont)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Already have an account?",
                fontFamily = colfiFont,
                fontSize = 12.sp,
                color = Color.Gray
            )
            TextButton(
                onClick = onNavigateToLogin
            ) {
                Text(
                    text = "Login",
                    fontFamily = colfiFont,
                    fontSize = 12.sp,
                    color = LightBrown1
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Benefits card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Sign up benefits:",
                    fontFamily = colfiFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• ₹150.55 welcome bonus",
                    fontFamily = colfiFont,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "• 100 reward points",
                    fontFamily = colfiFont,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "• 1 free voucher",
                    fontFamily = colfiFont,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}