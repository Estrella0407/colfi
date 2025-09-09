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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.colfi.R
import com.example.colfi.ui.theme.LightBrown2
import com.example.colfi.ui.theme.LightCream1
import com.example.colfi.ui.theme.colfiFont
import com.example.colfi.ui.viewmodel.PickUpViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PickUpScreen(
    userName: String,
    onBackClick: () -> Unit,
    onOrderNow: () -> Unit,
    viewModel: PickUpViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.orderItemPrice) {
        if (uiState.orderItemPrice > 0) {
            viewModel.updateTotals(uiState.orderItemPrice)
        }
    }
    val times = remember { viewModel.generateFuturePickUpTimes() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightCream1)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Row
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Button(
                    onClick = { /* already on pick up */ },
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
            }

            // Store Info
            Text(
                text = uiState.storeName,
                fontFamily = colfiFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = uiState.storeAddress,
                fontFamily = colfiFont,
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Select Pick Up Time
            var expanded by remember { mutableStateOf(false) }

            Box {
                Text(
                    text = if (uiState.selectedTime.isNotEmpty())
                        "Pick Up Time: ${uiState.selectedTime}"
                    else
                        "Select Pick Up Time ▼",
                    fontFamily = colfiFont,
                    fontSize = 14.sp,
                    color = Color(0xFF6B4F3B),
                    modifier = Modifier.clickable { expanded = true }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    times.forEach { time ->
                        DropdownMenuItem(
                            text = { Text(time, fontFamily = colfiFont) },
                            onClick = {
                                viewModel.selectTime(time)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Order Section
            Text(
                text = "Your Order",
                fontFamily = colfiFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = "https://via.placeholder.com/80x80.png?text=Drink",
                        contentDescription = uiState.orderItemName,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = uiState.orderItemName,
                            fontFamily = colfiFont,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "RM ${String.format("%.2f", uiState.orderItemPrice)}",
                            fontFamily = colfiFont,
                            color = Color.Black
                        )
                    }
                }
                IconButton(
                    onClick = { /* edit order */ },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.edit_icon),
                        contentDescription = "Edit Order",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Methods
            Text(
                text = "Payment Methods",
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
                    Text(
                        text = method,
                        fontFamily = colfiFont,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Payment Details
            Text(
                text = "Payment Details",
                fontFamily = colfiFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                PaymentDetailRow("Subtotal", "RM ${String.format("%.2f", uiState.subtotal)}")
                PaymentDetailRow("Service Tax 6%", "RM ${String.format("%.2f", uiState.serviceTax)}")
                PaymentDetailRow("Net Total", "RM ${String.format("%.2f", uiState.netTotal)}", isBold = true)
            }
        }

        // Bottom Order Button
        Button(
            onClick = onOrderNow,
            colors = ButtonDefaults.buttonColors(containerColor = LightBrown2),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .height(50.dp)
        ) {
            Text(
                text = "Order Now",
                fontFamily = colfiFont,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
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
