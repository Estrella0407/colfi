package com.example.colfi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colfi.data.model.CartItem
import com.example.colfi.ui.theme.colfiFont
import com.example.colfi.ui.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    userName: String,
    onBack: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: CartViewModel
) {
    val cartItems = viewModel.cartItems
    val totalPrice = viewModel.getTotalPrice()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Your Cart",
                        fontFamily = colfiFont,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            if (cartItems.isEmpty()) {
                // Empty cart message
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Your cart is empty",
                        fontFamily = colfiFont,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            } else {
                // List of cart items
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cartItems) { item ->
                        CartItemCard(
                            cartItem = item,
                            onRemove = { viewModel.removeFromCart(item) }
                        )
                    }
                }

                // Bottom bar (Total & Buttons)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F8F8))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total:",
                            fontFamily = colfiFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "RM ${String.format("%.2f", totalPrice)}",
                            fontFamily = colfiFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFFD2B48C)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Checkout button
                    Button(
                        onClick = { println("Checkout pressed") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD2B48C)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Checkout",
                            fontFamily = colfiFont,
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick navigation buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(onClick = onNavigateToMenu) {
                            Text("Back to Menu", fontFamily = colfiFont)
                        }
                        OutlinedButton(onClick = onNavigateToHome) {
                            Text("Home", fontFamily = colfiFont)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    cartItem: CartItem,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = cartItem.menuItem.name,
                    fontFamily = colfiFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = cartItem.option, // Hot or Ice
                    fontFamily = colfiFont,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Qty: ${cartItem.quantity}",
                    fontFamily = colfiFont,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "RM ${String.format("%.2f", cartItem.totalPrice)}",
                    fontFamily = colfiFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFFD2B48C)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onRemove) {
                    Text("Remove", color = Color.Red, fontFamily = colfiFont)
                }
            }
        }
    }
}
