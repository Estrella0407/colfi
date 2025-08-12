// LoadingScreen.kt
package com.example.colfi.ui.screens

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.colfi.R
import com.example.colfi.ui.theme.DarkBrown1
import com.example.colfi.ui.theme.LightCream1
import com.example.colfi.ui.theme.colfiFont
import com.example.colfi.ui.viewmodel.LoadingViewModel

@Composable
fun LoadingScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: LoadingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "loading_transition")

    LaunchedEffect(Unit) {
        viewModel.startLoading(onNavigateToLogin)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightCream1),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.character1),
                contentDescription = "Colfi Logo",
                modifier = Modifier
                    .size(120.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "COLFi",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBrown1,
                fontFamily = colfiFont,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Fall in Love, One Sip at a Time.",
                fontSize = 16.sp,
                color = DarkBrown1,
                fontFamily = colfiFont
            )
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(
                color = DarkBrown1,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}