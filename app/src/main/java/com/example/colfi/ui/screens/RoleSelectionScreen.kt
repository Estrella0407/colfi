package com.example.colfi.ui.screens

import android.R.attr.onClick
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun RoleSelectionScreen(
    onCustomerClick: () -> Unit,
    onStaffClick: () -> Unit,
    viewModel: RoleSelectionViewModel = viewModel()
) {
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
                text = "Which character are you?",
                fontFamily = colfiFont,
                fontSize = 24.sp,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            CharacterCard(
                R.drawable.coffee_lover,
                "Customer", "I'm a coffee lover",
                "Get your caffeine fix while staying, get it on the go or have it delivered to your home",
                onCustomerClick)

            Spacer(modifier = Modifier.height(32.dp))

            CharacterCard(
                R.drawable.barista,
                "Barista",
                "I'm a coffee maker",
                "Use your crafty hands to bre some happiness with some touch of caffeine",
                onStaffClick
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
    onclick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = LightCream1,
            contentColor = DarkBrown1
        ),
        modifier = Modifier
            .clickable { onclick() }
            .padding(8.dp),
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = roleTitle,
                    fontFamily = colfiFont,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    fontFamily = colfiFont,
                )
            }
        }
    }
}