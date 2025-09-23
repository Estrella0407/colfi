// DineInScreen.kt
package com.example.colfi.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.colfi.data.model.TableEntity
import com.example.colfi.ui.theme.DarkBrown1
import com.example.colfi.ui.theme.LightBrown1
import com.example.colfi.ui.theme.LightBrown2
import com.example.colfi.ui.theme.LightCream1
import com.example.colfi.ui.theme.colfiFont
import com.example.colfi.ui.viewmodel.DineInViewModel
import com.example.colfi.ui.viewmodel.DineInViewModelFactory

@Composable
fun DineInScreen(
    context: Context,
    onBackClick: () -> Unit,
    viewModel: DineInViewModel = viewModel(factory = DineInViewModelFactory(context))
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedTableId by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showNotAvailableDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightCream1)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // ✅ Header Row
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

            // ✅ Dine In button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { /* already on dine in */ },
                    colors = ButtonDefaults.buttonColors(containerColor = LightBrown2),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.width(100.dp)
                ) {
                    Text(
                        text = "Dine In",
                        fontFamily = colfiFont,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Grid layout with spacing
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.tables) { table: TableEntity ->
                    TableCard(
                        tableId = table.tableId,
                        isAvailable = table.isAvailable,
                        onClick = {
                            selectedTableId = table.tableId
                            if (table.isAvailable) {
                                showConfirmDialog = true
                            } else {
                                showNotAvailableDialog = true
                            }
                        }
                    )
                }
            }
        }

        // ✅ Confirm booking dialog
        if (showConfirmDialog && selectedTableId != null) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("Confirm Booking") },
                text = { Text("Do you want to book Table ${selectedTableId}?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.updateTableStatus(selectedTableId!!, false) // mark as booked
                        showConfirmDialog = false
                    }) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Not available dialog
        if (showNotAvailableDialog && selectedTableId != null) {
            AlertDialog(
                onDismissRequest = { showNotAvailableDialog = false },
                title = { Text("Table Not Available") },
                text = { Text("Sorry, Table ${selectedTableId} is already booked.") },
                confirmButton = {
                    TextButton(onClick = { showNotAvailableDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun TableCard(tableId: String, isAvailable: Boolean, onClick: () -> Unit) {
    val bgColor = if (isAvailable) LightBrown1 else DarkBrown1
    val textColor = Color.White

    Box(
        modifier = Modifier
            .size(100.dp)
            .background(bgColor, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text("Table $tableId", color = textColor)
    }
}
