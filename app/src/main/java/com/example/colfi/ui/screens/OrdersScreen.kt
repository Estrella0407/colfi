// OrdersScreen.kt
package com.example.colfi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.colfi.data.model.OrderHistory
import com.example.colfi.data.model.OrderItem
import com.example.colfi.ui.theme.LightBrown2
import com.example.colfi.ui.theme.colfiFont
import com.example.colfi.ui.viewmodel.OrdersViewModel

@Composable
fun OrdersScreen(
    onNavigateToMenu: () -> Unit,
    onNavigateToOrders: () -> Unit,
    viewModel: OrdersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding() // Ensure navigation bar padding is applied
            .background(Color(0xFFF5F5DC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 56.dp)
        ) {
            OrderHeader(selectedTab = selectedTab, onTabSelected = { viewModel.selectTab(it) })

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFFD2B48C)
                        )
                    }
                    uiState.errorMessage.isNotEmpty() -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error loading orders",
                                fontFamily = colfiFont,
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = uiState.errorMessage,
                                fontFamily = colfiFont,
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                    uiState.orders.isEmpty() -> {
                        Text(
                            text = if (selectedTab == "current") "No current orders" else "No order history",
                            modifier = Modifier.align(Alignment.Center),
                            fontFamily = colfiFont,
                            color = Color.Gray
                        )
                    }
                    else -> {
                        OrdersList(
                            orders = uiState.orders,
                            isCurrentOrder = selectedTab == "current",
                            onCancelOrder = { orderId -> viewModel.cancelOrder(orderId) }
                        )
                    }
                }
            }
        }

        // Bottom Navigation Bar
        BottomNavigation(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            onMenuClick = onNavigateToMenu,
            onOrdersClick = onNavigateToOrders
        )
    }
}


@Composable
fun OrderHeader(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    Column {
        // Title row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5DC))
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = "— COLFi —",
                fontFamily = colfiFont,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Order History",
                fontFamily = colfiFont,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        // Tab buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5DC))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TabButton(
                text = "Current Order",
                isSelected = selectedTab == "current",
                onClick = { onTabSelected("current") },
                modifier = Modifier.weight(1f)
            )

            TabButton(
                text = "History",
                isSelected = selectedTab == "history",
                onClick = { onTabSelected("history") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) LightBrown2 else Color.Gray.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = text,
            fontFamily = colfiFont,
            fontSize = 14.sp,
            color = Color.Black,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun OrdersList(
    orders: List<OrderHistory>,
    isCurrentOrder: Boolean,
    onCancelOrder: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(orders) { order ->
            OrderCard(
                order = order,
                isCurrentOrder = isCurrentOrder,
                onCancelOrder = onCancelOrder
            )
        }
    }
}

@Composable
fun OrderCard(
    order: OrderHistory,
    isCurrentOrder: Boolean,
    onCancelOrder: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5DC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Order header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${order.orderId} • ${getOrderTypeDisplay(order.orderStatus)}",
                    fontFamily = colfiFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = formatTime(order.orderDate),
                    fontFamily = colfiFont,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Address (for delivery orders)
            if (order.orderStatus.contains("delivery", ignoreCase = true) || order.orderId.contains("delivery", ignoreCase = true)) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Jalan Taman Ibu Kota 4 Jalan 14/6, Tmn. Taman Setapak Indah,",
                            fontFamily = colfiFont,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "53300 Kuala Lumpur (Jenny)",
                            fontFamily = colfiFont,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Driver info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Driver : Tan Ah Beng",
                        fontFamily = colfiFont,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "012-34567891",
                        fontFamily = colfiFont,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• Delivery Instruction: Leave at the door. No need to knock",
                    fontFamily = colfiFont,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Order status
            if (isCurrentOrder) {
                Text(
                    text = getStatusMessage(order.orderStatus),
                    fontFamily = colfiFont,
                    fontSize = 14.sp,
                    color = when (order.orderStatus.lowercase()) {
                        "preparing" -> Color(0xFFFF9800)
                        "ready" -> Color(0xFF4CAF50)
                        "delivering" -> Color(0xFF2196F3)
                        else -> Color.Gray
                    },
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Order items
            order.orderItems.forEach { item ->
                OrderItemRow(item = item)
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Order total and cancel button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Subtotal",
                        fontFamily = colfiFont,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "RM ${String.format("%.2f", order.orderTotal)}",
                        fontFamily = colfiFont,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    if (isCurrentOrder && order.orderStatus.lowercase() in listOf("pending", "preparing")) {
                        Button(
                            onClick = { onCancelOrder(order.orderId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LightBrown2
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = "Cancel Order",
                                fontFamily = colfiFont,
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItemRow(item: OrderItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${item.name} ${if (item.temperature.isNotEmpty()) "(${item.temperature})" else ""}",
            fontFamily = colfiFont,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "x${item.quantity}",
            fontFamily = colfiFont,
            fontSize = 14.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.width(24.dp))

        Text(
            text = "RM ${String.format("%.2f", item.price * item.quantity)}",
            fontFamily = colfiFont,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

private fun getOrderTypeDisplay(status: String): String {
    return when (status.lowercase()) {
        "pending", "preparing", "ready" -> "Pickup"
        "delivering" -> "Delivery"
        else -> "Delivery"
    }
}

private fun getStatusMessage(status: String): String {
    return when (status.lowercase()) {
        "pending" -> "Order received! Preparing your order..."
        "preparing" -> "Getting your order ready!"
        "ready" -> "Your order is ready for pickup!"
        "delivering" -> "Your order is on the way!"
        else -> "Getting your order ready!"
    }
}

private fun formatTime(dateString: String): String {
    // This is a simple implementation. You might want to use proper date formatting
    // based on your actual date format from Firebase
    return try {
        // Assuming the format is something like "2024-01-15 11:15:00"
        val time = dateString.split(" ").getOrNull(1)?.substring(0, 5) ?: "11:15"
        "$time AM"
    } catch (e: Exception) {
        "11:15 AM"
    }
}