// PickUpScreen.kt
package com.example.colfi.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.colfi.ui.theme.LightBrown2
import com.example.colfi.ui.theme.LightCream1
import com.example.colfi.ui.theme.colfiFont
import com.example.colfi.ui.viewmodel.CartViewModel
import com.example.colfi.ui.viewmodel.PickUpViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.colfi.R

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PickUpScreen(
    userName: String,
    cartViewModel: CartViewModel,
    onBackClick: () -> Unit,
    onOrderNow: () -> Unit,
    onEditOrderClick: () -> Unit,
    viewModel: PickUpViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val cartUiState by cartViewModel.uiState.collectAsState()
    val cartItems = cartUiState.cartItems

    var showSuccessDialog by remember { mutableStateOf(false) }

    // Update totals whenever cart changes
    LaunchedEffect(cartItems) {
        viewModel.updateTotals(cartItems)
    }

    val times = remember { viewModel.generateFuturePickUpTimes() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightCream1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }

                    Text(
                        text = "— COLFi —",
                        fontFamily = colfiFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pick Up button (current screen)
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = LightBrown2),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.width(100.dp)
                ) {
                    Text(
                        text = "Pick Up",
                        fontFamily = colfiFont,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Store info
                Text(
                    text = uiState.storeName,
                    fontFamily = colfiFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = uiState.storeAddress,
                    fontFamily = colfiFont,
                    fontSize = 15.sp,
                    color = Color.Gray
                )

                val storeLocation = LatLng(uiState.storeLat, uiState.storeLng)
                val cameraPositionState = rememberCameraPositionState {
                    position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(storeLocation, 15f)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    GoogleMap(
                        modifier = Modifier.matchParentSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        Marker(
                            state = MarkerState(position = storeLocation),
                            title = uiState.storeName,
                            snippet = "Our Store Location"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Pick Up time
                var expanded by remember { mutableStateOf(false) }
                Box {
                    Text(
                        text = if (uiState.selectedTime.isNotEmpty()) "Pick Up Time: ${uiState.selectedTime}" else "Select Pick Up Time ▼",
                        fontFamily = colfiFont,
                        fontSize = 14.sp,
                        color = Color(0xFF6B4F3B),
                        modifier = Modifier.clickable { expanded = true }
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        times.forEach { time ->
                            DropdownMenuItem(text = { Text(time, fontFamily = colfiFont) }, onClick = {
                                viewModel.selectTime(time)
                                expanded = false
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Your Order Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Your Order",
                        fontFamily = colfiFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    IconButton(
                        onClick = onEditOrderClick,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.edit_icon),
                            contentDescription = "Edit Order",
                            modifier = Modifier.size(48.dp)
                        )
                    }

                }

                // Cart items
                cartItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = "https://via.placeholder.com/80x80.png?text=Drink",
                                contentDescription = item.menuItem.name,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = item.menuItem.name,
                                    fontFamily = colfiFont,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "RM ${String.format("%.2f", item.totalPrice)}",
                                    fontFamily = colfiFont,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Payment methods
                Text(
                    "Payment Methods",
                    fontFamily = colfiFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                val methods = listOf("Credit Card", "Colfi Wallet", "E-wallet", "Cash")
                methods.forEach { method ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { viewModel.selectPaymentMethod(method) }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = uiState.paymentMethod == method,
                            onClick = { viewModel.selectPaymentMethod(method) }
                        )
                        Text(text = method, fontFamily = colfiFont, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Payment details
                Text(
                    "Payment Details",
                    fontFamily = colfiFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Column(modifier = Modifier.fillMaxWidth()) {
                    PaymentDetailRow("Subtotal", "RM ${String.format("%.2f", uiState.subtotal)}")
                    PaymentDetailRow("Service Tax 6%", "RM ${String.format("%.2f", uiState.serviceTax)}")
                    PaymentDetailRow("Net Total", "RM ${String.format("%.2f", uiState.netTotal)}", isBold = true)
                }

                Spacer(modifier = Modifier.height(16.dp)) // Extra space so button does not overlap
            }

            // Bottom Order Now button
            Button(
                onClick = { showSuccessDialog = true; onOrderNow() },
                colors = ButtonDefaults.buttonColors(containerColor = LightBrown2),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    "Order Now",
                    fontFamily = colfiFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        if (showSuccessDialog) {
            LaunchedEffect(Unit) { delay(3000); showSuccessDialog = false }
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                confirmButton = {},
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.successful),
                            contentDescription = "Success",
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Placed Successfully",
                            fontFamily = colfiFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                containerColor = LightCream1
            )
        }
    }
}

@Composable
fun PaymentDetailRow(label: String, amount: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = colfiFont,
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = amount,
            fontFamily = colfiFont,
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}
