// DeliveryScreen.kt
package com.example.colfi.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.colfi.R
import com.example.colfi.ui.theme.*
import com.example.colfi.ui.viewmodel.CartViewModel
import com.example.colfi.ui.viewmodel.PickUpViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.res.painterResource
import com.example.colfi.DrawableMapper
import com.example.colfi.data.model.MenuItem
import com.example.colfi.ui.viewmodel.CheckoutViewModel
import com.example.colfi.ui.viewmodel.DeliveryViewModel

@Composable
fun DeliveryScreen(
    userName: String,
    cartViewModel: CartViewModel,
    onBackClick: () -> Unit,
    onOrderNow: () -> Unit,
    onEditOrderClick: () -> Unit,
    viewModel: DeliveryViewModel = viewModel(),
    checkoutViewModel: CheckoutViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val cartUiState by cartViewModel.uiState.collectAsState()
    val cartItems = cartUiState.cartItems

    val menuItem = MenuItem()

    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showAddressPopup by remember { mutableStateOf(false) }
    var showInstructionPopup by remember { mutableStateOf(false) }
    var savedAddresses by rememberSaveable { mutableStateOf(listOf<String>()) }

    // Sync all data with CheckoutViewModel
    LaunchedEffect(uiState.customerAddress, uiState.paymentMethod, userName) {
        // Update customer info
        checkoutViewModel.updateCustomerInfo(
            name = userName.ifEmpty { "Guest User" },
            phone = "" // You'll need to get this from somewhere
        )

        // Update order type
        checkoutViewModel.updateOrderType("delivery")

        // Update delivery address (THIS IS WHAT'S MISSING!)
        checkoutViewModel.updateDeliveryAddress(uiState.customerAddress)

        // Update payment method
        checkoutViewModel.updatePaymentMethod(uiState.paymentMethod)
    }

    // Also sync delivery instructions if needed
    LaunchedEffect(uiState.deliveryInstruction) {
        checkoutViewModel.updateSpecialInstructions(uiState.deliveryInstruction)
    }

    // Update totals whenever cart changes
    LaunchedEffect(cartItems) {
        viewModel.updateTotals(cartItems)
    }

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
                .padding(16.dp)
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .weight(1f) // 👈 makes scrollable part take remaining space
                    .fillMaxWidth()
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

                // Delivery Address
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

                //Your Order
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
                            Image(
                                painter = painterResource(
                                    id = if (menuItem.imageName.isNotEmpty()) {
                                        menuItem.imageResId
                                    } else {
                                        DrawableMapper.getDrawableForImageName(menuItem.category)
                                    }
                                ),
                                contentDescription = menuItem.name,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
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

                // Delivery Instruction Section
                Text(
                    text = "Delivery Instruction",
                    fontFamily = colfiFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                if (uiState.deliveryInstruction.isNullOrEmpty()) {
                    Text(
                        text = "+ Add Instruction",
                        fontFamily = colfiFont,
                        color = DarkBrown1,
                        modifier = Modifier
                            .clickable { showInstructionPopup = true }
                            .padding(vertical = 4.dp)
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
                            text = "Edit Instruction",
                            color = Color.Gray,
                            fontFamily = colfiFont,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { showInstructionPopup = true }
                        )
                    }
                }

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
                    PaymentDetailRow(
                        "Service Tax 6%",
                        "RM ${String.format("%.2f", uiState.serviceTax)}"
                    )
                    PaymentDetailRow(
                        "Net Total",
                        "RM ${String.format("%.2f", uiState.netTotal)}",
                        isBold = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Order Now button stays visible
            val isFormValid by remember(uiState.customerAddress, uiState.paymentMethod) {
                derivedStateOf {
                    uiState.customerAddress.isNotBlank() && uiState.paymentMethod.isNotBlank()
                }
            }

            val errorText by remember(uiState.customerAddress, uiState.paymentMethod) {
                derivedStateOf {
                    when {
                        uiState.customerAddress.isBlank() -> "Delivery address is required"
                        uiState.paymentMethod.isBlank() -> "Please select a payment method"
                        else -> null
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                // Show error message if form is invalid
                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        color = Color.Red,
                        fontFamily = colfiFont,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        if (isFormValid) {
                            checkoutViewModel.placeOrder(
                                cartItems = cartItems,
                                onSuccess = {
                                    showSuccessDialog = true
                                    cartViewModel.clearCart()
                                },
                                onFailure = { msg: String ->
                                    errorMessage = msg
                                    showErrorDialog = true
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFormValid) LightBrown2 else Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = isFormValid
                ) {
                    Text(
                        if (isFormValid) "Order Now" else "Complete Form to Order",
                        fontFamily = colfiFont,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // dialogs + popups remain same
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

            if (showErrorDialog) {
                AlertDialog(
                    onDismissRequest = { showErrorDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showErrorDialog = false }) {
                            Text("OK", fontFamily = colfiFont)
                        }
                    },
                    text = {
                        Text(
                            text = errorMessage,
                            fontFamily = colfiFont,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    containerColor = LightCream1
                )
            }

            // Address Popup
            if (showAddressPopup) {
                AddressPopup(
                    onDismiss = { showAddressPopup = false },
                    onSelect = { selected -> viewModel.setCustomerAddress(selected) },
                    onSave = { newAddress ->
                        savedAddresses = savedAddresses + newAddress
                        viewModel.setCustomerAddress(newAddress)
                    },
                    savedAddresses = savedAddresses
                )
            }

            // Instruction Popup
            if (showInstructionPopup) {
                InstructionPopup(
                    currentInstruction = uiState.deliveryInstruction,
                    onDismiss = { showInstructionPopup = false },
                    onSave = { viewModel.setDeliveryInstruction(it) }
                )
            }
        }
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
        title = { Text("Manage Addresses", color = DarkBrown1, fontFamily = colfiFont) },
        text = {
            Column {
                OutlinedTextField(
                    value = newAddress,
                    onValueChange = {
                        newAddress = it
                        if (isError && it.isNotBlank()) isError = false
                    },
                    label = { Text("Enter Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    isError = isError
                )

                if (isError) Text("Address cannot be empty!", color = Color.Red, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (newAddress.isNotBlank()) {
                            onSave(newAddress)
                            newAddress = ""
                            isError = false
                            onDismiss()
                        } else isError = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = LightBrown2)
                ) { Text("Save Address", color = DarkBrown1, fontFamily = colfiFont) }

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
                            Text(
                                "Use",
                                color = DarkBrown1,
                                modifier = Modifier.clickable {
                                    onSelect(address)
                                    onDismiss()
                                }
                            )
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
            Button(onClick = {
                onSave(instruction)
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Delivery Instruction", color = DarkBrown1, fontFamily = colfiFont) },
        text = {
            OutlinedTextField(
                value = instruction,
                onValueChange = { instruction = it },
                label = { Text("Enter instruction") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        containerColor = LightCream1
    )
}
