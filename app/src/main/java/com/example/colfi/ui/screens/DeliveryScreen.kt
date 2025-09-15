//Delivery Screen.kt
package com.example.colfi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.colfi.R
import com.example.colfi.ui.theme.DarkBrown1
import com.example.colfi.ui.theme.LightBrown1
import com.example.colfi.ui.theme.LightBrown2
import com.example.colfi.ui.theme.LightCream1
import com.example.colfi.ui.theme.colfiFont
import com.example.colfi.ui.viewmodel.DeliveryViewModel
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.res.painterResource


@Composable
fun DeliveryScreen(
    userName: String,
    onBackClick: () -> Unit,
    onOrderNow: () -> Unit,
    viewModel: DeliveryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showAddressPopup by remember { mutableStateOf(false) }
    var showInstructionPopup by remember { mutableStateOf(false) }
    var savedAddresses by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(uiState.orderItemPrice) {
        if (uiState.orderItemPrice > 0) {
            viewModel.updateTotals(uiState.orderItemPrice)
        }
    }

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

            // Delivery Address Section
            Text(
                text = "Delivery Address",
                fontFamily = colfiFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            if (uiState.customerAddress.isNullOrEmpty()) {
                Text(
                    text = "+ Add Address",
                    fontFamily = colfiFont,
                    color = DarkBrown1,
                    modifier = Modifier.clickable { showAddressPopup = true }
                )
            } else {
                Column {
                    Text(
                        text = uiState.customerAddress,
                        fontFamily = colfiFont,
                        fontSize = 15.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Change Address",
                        color = Color.Gray,
                        fontFamily = colfiFont,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { showAddressPopup = true }
                    )
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = {},
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
            }

            Spacer(modifier = Modifier.height(16.dp))


            Text(
                text = "Delivery Instruction",
                fontFamily = colfiFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            if (uiState.deliveryInstruction.isEmpty()) {
                Text(
                    text = "+ Add Instruction",
                    fontFamily = colfiFont,
                    color = DarkBrown1,
                    modifier = Modifier.clickable { showInstructionPopup = true }
                )
            } else {
                Column {
                    Text(
                        text = uiState.deliveryInstruction,
                        fontFamily = colfiFont,
                        fontSize = 15.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Change Instruction",
                        color = Color.Gray,
                        fontFamily = colfiFont,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { showInstructionPopup = true }
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
            onClick = {
                onOrderNow()
                showSuccessDialog = true
            },
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
        if (showSuccessDialog) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(3000)
                showSuccessDialog = false
            }

            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                confirmButton = {},
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Success Image
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

    // Address Popup
    if (showAddressPopup) {
        AddressPopup(
            onDismiss = { showAddressPopup = false },
            onSelect = { selected ->
                viewModel.setCustomerAddress(selected)
                showAddressPopup = false
            },
            onSave = { newAddress ->
                savedAddresses = savedAddresses + newAddress
                viewModel.setCustomerAddress(newAddress)
                showAddressPopup = false
            },
            savedAddresses = savedAddresses
        )
    }

    // Instruction Popup
    if (showInstructionPopup) {
        InstructionPopup(
            currentInstruction = uiState.deliveryInstruction,
            onDismiss = { showInstructionPopup = false },
            onSave = { instruction ->
                viewModel.setDeliveryInstruction(instruction)
                showInstructionPopup = false
            }
        )
    }
}

@Composable
fun AddressPopup(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    onSave: (String) -> Unit,
    savedAddresses: List<String>
) {
    var newAddress by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = DarkBrown1, fontFamily = colfiFont)
            }
        },
        title = {
            Text("Manage Addresses", color = DarkBrown1, fontFamily = colfiFont)
        },
        text = {
            Column {
                // Input field for address
                OutlinedTextField(
                    value = newAddress,
                    onValueChange = {
                        newAddress = it
                        if (isError && it.isNotBlank()) {
                            isError = false // clear error once user types
                        }
                    },
                    label = { Text("Enter Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    isError = isError,
                    colors = TextFieldDefaults.colors(
                        //container color
                        focusedContainerColor = LightCream1,
                        unfocusedContainerColor = LightCream1,
                        disabledContainerColor = LightCream1,
                        errorContainerColor = LightCream1,

                        // Text color
                        focusedTextColor = DarkBrown1,
                        unfocusedTextColor = DarkBrown1,
                        disabledTextColor = Color.Gray,

                        // Cursor
                        cursorColor = LightBrown2,

                        focusedIndicatorColor = LightBrown2,  // focusedBorderColor
                        unfocusedIndicatorColor = DarkBrown1, // unfocusedBorderColor
                        disabledIndicatorColor = Color.LightGray,
                        errorIndicatorColor = Color.Red, // Example for error state

                        // Label color
                        focusedLabelColor = LightBrown2,
                        unfocusedLabelColor = DarkBrown1,
                        disabledLabelColor = Color.Gray,
                    )
                )

                if (isError) {
                    Text(
                        text = "Address cannot be empty!",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (newAddress.isNotBlank()) {
                            onSave(newAddress)
                            newAddress = ""
                            isError = false
                        } else {
                            isError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = LightBrown2)
                ) {
                    Text("Save Address", color = DarkBrown1, fontFamily = colfiFont)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (savedAddresses.isNotEmpty()) {
                    Text("Saved Addresses:")
                    savedAddresses.forEach { address ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(address) }
                                .padding(4.dp)
                        ) {
                            Text("• $address", modifier = Modifier.weight(1f))
                            Text("Use", color = DarkBrown1)
                        }
                    }
                }
            }
        },
        containerColor = LightCream1
    )
}

@Composable
fun InstructionPopup(
    currentInstruction: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var instruction by remember { mutableStateOf(currentInstruction) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onSave(instruction) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text("Delivery Instruction",
            color = DarkBrown1,
            fontFamily = colfiFont)},
        text = {
            OutlinedTextField(
                value = instruction,
                onValueChange = { instruction = it },
                label = {
                    Text("Enter instruction")},
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    //container color
                    focusedContainerColor = LightCream1,
                    unfocusedContainerColor = LightCream1,
                    disabledContainerColor = LightCream1,
                    errorContainerColor = LightCream1,

                    // Text color
                    focusedTextColor = DarkBrown1,
                    unfocusedTextColor = DarkBrown1,
                    disabledTextColor = Color.Gray,

                    // Cursor
                    cursorColor = LightBrown2,

                    focusedIndicatorColor = LightBrown2,  // focusedBorderColor
                    unfocusedIndicatorColor = DarkBrown1, // unfocusedBorderColor
                    disabledIndicatorColor = Color.LightGray,
                    errorIndicatorColor = Color.Red, // Example for error state

                    // Label color
                    focusedLabelColor = LightBrown2,
                    unfocusedLabelColor = DarkBrown1,
                    disabledLabelColor = Color.Gray,

                    )
            )
        },
        containerColor = LightCream1
    )
}