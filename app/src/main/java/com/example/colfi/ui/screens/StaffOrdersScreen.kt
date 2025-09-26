// StaffOrdersScreen.kt
package com.example.colfi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colfi.R
import com.example.colfi.ui.theme.DarkBrown1
import com.example.colfi.ui.theme.LightCream1
import com.example.colfi.ui.theme.colfiFont
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.colfi.ui.viewmodel.StaffOrdersViewModel
import com.example.colfi.data.model.OrderHistory
import com.example.colfi.ui.theme.LightBrown1
import com.example.colfi.ui.theme.LightBrown2
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun StaffOrdersScreen(
    userName: String,
    onNavigateToProducts: () -> Unit,
    onNavigateToStaffProfile: () -> Unit,
    viewModel: StaffOrdersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val swipeRefreshState = rememberSwipeRefreshState(uiState.isRefreshing)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(LightCream1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 56.dp)
        ) {
            OngoingOrdersHeader(
                selectedTab = selectedTab,
                onTabSelected = { viewModel.selectTab(it) },
                orderCounts = uiState.orderCounts
            )

            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = { viewModel.refreshOrders() }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    when {
                        uiState.isLoading && uiState.orders.isEmpty() -> {
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
                                Button(
                                    onClick = { viewModel.clearError(); viewModel.refreshOrders() },
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                        uiState.filteredOrders.isEmpty() -> {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No orders found",
                                    fontFamily = colfiFont,
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = when (selectedTab) {
                                        "dine_in" -> "No dine-in orders at the moment"
                                        "pick_up" -> "No pick-up orders at the moment"
                                        "delivery" -> "No delivery orders at the moment"
                                        else -> "No orders at the moment"
                                    },
                                    fontFamily = colfiFont,
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(uiState.filteredOrders) { order ->
                                    OrderCard(
                                        order = order,
                                        onStatusUpdate = { orderId, status ->
                                            viewModel.updateOrderStatus(orderId, status)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Navigation Bar
        StaffBottomNavigation(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            onStaffOrdersClick = { },
            onProductsClick = onNavigateToProducts,
            onStaffProfileClick = onNavigateToStaffProfile,
            isStaffOrdersSelected = true,
            isProductsSelected = false,
            isStaffProfileSelected = false
        )
    }
}

@Composable
fun OrderCard(
    order: OrderHistory,
    onStatusUpdate: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Order Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.orderId.take(6)}",
                    fontFamily = colfiFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = order.statusColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = order.orderStatus.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = order.customerName,
                        fontFamily = colfiFont,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Text(
                        text = order.customerPhone,
                        fontFamily = colfiFont,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = order.orderType.replace("_", " ").uppercase(),
                        fontFamily = colfiFont,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = DarkBrown1
                    )
                    Text(
                        text = order.formattedOrderDate,
                        fontFamily = colfiFont,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Order Items
            order.orderItems.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.quantity}x ${item.name}",
                        fontFamily = colfiFont,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = String.format("RM %.2f", item.price * item.quantity),
                        fontFamily = colfiFont,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (item.temperature.isNotEmpty() || item.customizations.isNotEmpty()) {
                    Text(
                        text = buildString {
                            if (item.temperature.isNotEmpty()) append("${item.temperature} ")
                            if (item.customizations.isNotEmpty()) {
                                append(item.customizations.joinToString(", "))
                            }
                        }.trim(),
                        fontFamily = colfiFont,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            // Total and Special Instructions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    fontFamily = colfiFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = order.formattedTotalAmount,
                    fontFamily = colfiFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            if (order.specialInstructions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Note: ${order.specialInstructions}",
                    fontFamily = colfiFont,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            // Action Buttons
            if (order.orderStatus in listOf("pending", "preparing", "ready")) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (order.orderStatus) {
                        "pending" -> {
                            Button(
                                onClick = { onStatusUpdate(order.orderId, "preparing") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = LightBrown1 )
                            ) {
                                Text(
                                    text = "Start Preparing",
                                    fontSize = 12.sp,
                                    fontFamily = colfiFont,
                                    color = Color.White
                                )
                            }
                        }
                        "preparing" -> {
                            Button(
                                onClick = { onStatusUpdate(order.orderId, "ready") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor =LightBrown2)
                            ) {
                                Text(
                                    text = "Mark Ready",
                                    fontSize = 12.sp,
                                    fontFamily = colfiFont
                                )
                            }
                        }
                        "ready" -> {
                            Button(
                                onClick = {
                                    val nextStatus = if (order.orderType == "delivery") "delivering" else "completed"
                                    onStatusUpdate(order.orderId, nextStatus)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkBrown1)
                            ) {
                                Text(
                                    text = if (order.orderType == "delivery") "Start Delivery" else "Complete",
                                    fontSize = 12.sp,
                                    fontFamily = colfiFont,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OngoingOrdersHeader(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    orderCounts: com.example.colfi.ui.state.OrderCounts
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
                text = "Ongoing Orders",
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
                text = "All (${orderCounts.all})",
                isSelected = selectedTab == "all",
                onClick = { onTabSelected("all") },
                modifier = Modifier.weight(1f)
            )

            TabButton(
                text = "Dine In (${orderCounts.dineIn})",
                isSelected = selectedTab == "dine_in",
                onClick = { onTabSelected("dine_in") },
                modifier = Modifier.weight(1f)
            )

            TabButton(
                text = "Pick Up (${orderCounts.pickUp})",
                isSelected = selectedTab == "pick_up",
                onClick = { onTabSelected("pick_up") },
                modifier = Modifier.weight(1f)
            )

            TabButton(
                text = "Delivery (${orderCounts.delivery})",
                isSelected = selectedTab == "delivery",
                onClick = { onTabSelected("delivery") },
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
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) DarkBrown1 else Color.Transparent,
            contentColor = if (isSelected) LightCream1 else DarkBrown1
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            fontFamily = colfiFont,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun StaffBottomNavigation(
    modifier: Modifier = Modifier,
    onStaffOrdersClick: () -> Unit = {},
    onProductsClick: () -> Unit = {},
    onStaffProfileClick: () -> Unit = {},
    isStaffOrdersSelected: Boolean = false,
    isProductsSelected: Boolean = false,
    isStaffProfileSelected: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = LightCream1,
            contentColor = DarkBrown1
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomNavItem(
                iconRes = R.drawable.order_history,
                label = "Orders",
                isSelected = isStaffOrdersSelected,
                onClick = onStaffOrdersClick
            )
            BottomNavItem(
                iconRes = R.drawable.product_management,
                label = "Products",
                isSelected = isProductsSelected,
                onClick = onProductsClick
            )
            BottomNavItem(
                iconRes = R.drawable.profile_icon,
                label = "Me",
                isSelected = isStaffProfileSelected,
                onClick = onStaffProfileClick
            )
        }
    }
}