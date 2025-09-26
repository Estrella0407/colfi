package com.example.colfi.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.colfi.data.model.OrderHistory
import com.example.colfi.data.model.OrderItem
import com.example.colfi.ui.theme.BackgroundColor
import com.example.colfi.ui.theme.LightBrown2
import com.example.colfi.ui.theme.LightCream1
import com.example.colfi.ui.theme.colfiFont
import com.example.colfi.ui.viewmodel.OrdersViewModel

@Composable
fun OrdersScreen(
    userName: String,
    onNavigateToMenu: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToCustomerProfile: () -> Unit,
    viewModel: OrdersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding() // Ensure navigation bar padding is applied
            .background(BackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 56.dp) // Adjust for bottom navigation height
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

        // Bottom Navigation Bar - Fixed at bottom
        BottomNavigation(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            onHomeClick = onNavigateToHome,
            onMenuClick = onNavigateToMenu,
            onOrdersClick = { },
            onCustomerProfileClick = onNavigateToCustomerProfile,
            isHomeSelected = false,
            isMenuSelected = false,
            isOrdersSelected = true,
            isCustomerProfileSelected = false
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
                .background(LightCream1)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Order History",
                fontFamily = colfiFont,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "— COLFi —",
                fontFamily = colfiFont,
                fontSize = 20.sp,
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
                    text = "#${order.orderId} • ${getOrderTypeDisplay(order.orderType)}",
                    fontFamily = colfiFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = formatTime(order.orderDate.toString()),
                    fontFamily = colfiFont,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Display different details based on order type
            when (order.orderType.lowercase()) {
                "dine_in" -> {
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Table ${order.tableNumber}",
                                fontFamily = colfiFont,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Show booking/arrival status
                    Text(
                        text = if (order.orderStatus.lowercase() == "pending") "Table reserved - Waiting for arrival"
                        else "Dine-in session active",
                        fontFamily = colfiFont,
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50), // Green color for active reservation
                        fontStyle = if (order.orderStatus.lowercase() == "pending") FontStyle.Italic else FontStyle.Normal
                    )
                }

                "delivery" -> {
                    // Delivery specific details
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
                                text = order.deliveryAddress
                                    ?: "Jalan Taman Ibu Kota 4 Jalan 14/6, Tmn. Taman Setapak Indah,",
                                fontFamily = colfiFont,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "53300 Kuala Lumpur (${order.customerName})",
                                fontFamily = colfiFont,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Driver info (only for delivering status)
                    if (order.orderStatus.lowercase() == "delivering") {
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
                    }

                    // Delivery instructions
                    if (!order.specialInstructions.isNullOrEmpty()) {
                        Text(
                            text = "• Delivery Instruction: ${order.specialInstructions}",
                            fontFamily = colfiFont,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                "pick_up" -> {
                    // Pick-up specific details
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Pickup Location",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Store Pickup - COLFi Coffee Shop",
                            fontFamily = colfiFont,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Estimated ready time for pick-up
                    if (isCurrentOrder && order.orderStatus.lowercase() in listOf(
                            "pending",
                            "preparing"
                        )
                    ) {
                        Text(
                            text = "Estimated ready in ${order.estimatedTime} minutes",
                            fontFamily = colfiFont,
                            fontSize = 11.sp,
                            color = Color(0xFFFF9800) // Orange color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Order status message
            if (isCurrentOrder) {
                Text(
                    text = getStatusMessage(order.orderStatus, order.orderType),
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

            // Order items - Show different message for dine-in bookings
            if (order.orderType.lowercase() == "dine_in" &&
                order.orderItems.any {
                    it.name.contains(
                        "table",
                        ignoreCase = true
                    ) || it.price == 0.0
                }
            ) {
                // Special display for table bookings
                Text(
                    text = "Table Reservation",
                    fontFamily = colfiFont,
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
                if (!order.tableNumber.isNullOrEmpty()) {
                    Text(
                        text = "Table ${order.tableNumber} - Ready for dining",
                        fontFamily = colfiFont,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            } else {
                // Regular order items display
                order.orderItems.forEach { item ->
                    OrderItemRow(item = item)
                    Spacer(modifier = Modifier.height(4.dp))
                }
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
                        text = if (order.orderType.lowercase() == "dine_in" && order.totalAmount == 0.0) "Reservation" else "Subtotal",
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
                        text = if (order.totalAmount > 0) "RM ${
                            String.format("%.2f", order.totalAmount)}" else "",
                        fontFamily = colfiFont,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    if (isCurrentOrder && order.orderStatus.lowercase() in listOf(
                            "pending",
                            "preparing"
                        )
                    ) {
                        Button(
                            onClick = { onCancelOrder(order.orderId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LightBrown2
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = if (order.orderType.lowercase() == "dine_in") "Cancel Booking" else "Cancel Order",
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

    private fun getOrderTypeDisplay(orderType: String): String {
        return when (orderType.lowercase()) {
            "dine_in" -> "Dine-in"
            "pick_up" -> "Pickup"
            "delivery" -> "Delivery"
            else -> orderType.replace("_", " ").capitalize()
        }
    }

    private fun getStatusMessage(status: String, orderType: String): String {
        return when {
            orderType.lowercase() == "dine_in" && status.lowercase() == "pending" ->
                "Table reserved! Waiting for your arrival..."
            orderType.lowercase() == "dine_in" && status.lowercase() == "preparing" ->
                "Dine-in session active! Your food is being prepared"
            orderType.lowercase() == "dine_in" && status.lowercase() == "ready" ->
                "Your table is ready with food served!"
            orderType.lowercase() == "dine_in" ->
                "Dine-in session in progress"
            status.lowercase() == "pending" -> "Order received! Preparing your order..."
            status.lowercase() == "preparing" -> "Getting your order ready!"
            status.lowercase() == "ready" -> "Your order is ready for pickup!"
            status.lowercase() == "delivering" -> "Your order is on the way!"
            else -> "Order processing..."
        }
    }

    private fun formatTime(orderDate: Any): String {
        return try {
            when (orderDate) {
                is Long -> {
                    // Handle timestamp (milliseconds)
                    val date = java.util.Date(orderDate)
                    val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                    sdf.format(date)
                }
                is String -> {
                    // Handle string date (your existing logic)
                    val time = orderDate.split(" ").getOrNull(1)?.substring(0, 5) ?: "11:15"
                    "$time AM"
                }
                else -> "11:15 AM"
            }
        } catch (e: Exception) {
            "11:15 AM"
        }
    }
