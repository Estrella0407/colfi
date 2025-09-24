package com.example.colfi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.colfi.data.model.CartItem
import com.example.colfi.ui.viewmodel.CheckoutViewModel

@Composable
fun CheckoutPopUp(
    cartItems: List<CartItem>,
    checkoutViewModel: CheckoutViewModel,
    onDismiss: () -> Unit,
    onNavigateToPickup: () -> Unit,
    onNavigateToDelivery: () -> Unit,
    
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {

                // Header: Checkout title + close icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Checkout",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(8.dp)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Orders list
                Text(
                    text = "Your Order",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                cartItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.menuItem.name} x${item.quantity}")
                        Text("RM ${String.format("%.2f", item.totalPrice)}")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Choose order type
                Text(
                    text = "Choose Order Type",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = onNavigateToPickup) {
                        Text("Pick Up")
                    }
                    Button(onClick = onNavigateToDelivery) {
                        Text("Delivery")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
