//Dine In Screen.kt
package com.example.colfi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colfi.ui.theme.DarkBrown1
import com.example.colfi.ui.theme.LightCream1
import com.example.colfi.ui.theme.colfiFont
import com.example.colfi.ui.viewmodel.DineInViewModel

@Composable
fun DineInScreen(
    viewModel: DineInViewModel,
    onTableClick: (String) -> Unit
) {
    val tables by viewModel.tables.collectAsState()
    val selectedTable by viewModel.selectedTable.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Select Your Table", fontFamily = colfiFont, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(24.dp))

        val tableRows = tables.chunked(3)
        tableRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { table ->
                    SquareButton(
                        label = table.tableId,
                        isSelected = table.tableId == selectedTable,
                        isAvailable = table.isAvailable,
                        onClick = {
                            if (table.isAvailable) {
                                viewModel.selectTable(table.tableId)
                                onTableClick(table.tableId)
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SquareButton(
    label: String,
    isSelected: Boolean,
    isAvailable: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(80.dp)
            .background(
                color = when {
                    !isAvailable -> Color.Gray
                    isSelected -> DarkBrown1
                    else -> LightCream1
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = isAvailable) { onClick() }
    ) {
        Text(
            text = label,
            fontFamily = colfiFont,
            color = if (!isAvailable || isSelected) Color.White else DarkBrown1,
            fontSize = 18.sp
        )
    }
}
