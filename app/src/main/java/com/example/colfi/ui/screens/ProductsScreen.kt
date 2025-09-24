// ProductsScreen.kt
package com.example.colfi.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.colfi.DrawableMapper
import com.example.colfi.data.model.MenuItem
import com.example.colfi.ui.state.ProductsUiState
import com.example.colfi.ui.theme.DarkBrown1
import com.example.colfi.ui.theme.LightBrown2
import com.example.colfi.ui.theme.LightCream1
import com.example.colfi.ui.theme.LightCream2
import com.example.colfi.ui.theme.colfiFont
import com.example.colfi.ui.viewmodel.ProductsViewModel

@Composable
fun ProductsScreen(
    onNavigateToStaffOrders: () -> Unit,
    onNavigateToStaffProfile: () -> Unit,
    viewModel: ProductsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
            // Header matching Figma design
            Text(
                text = "Product Management — COLFi —",
                fontFamily = colfiFont,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Category dropdown and availability indicator
            ProductsHeader(viewModel, uiState)

            // Product list (using your existing filtering)
            ProductList(products = uiState.filteredItems)
        }

        StaffBottomNavigation(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            onStaffOrdersClick = onNavigateToStaffOrders,
            onProductsClick = { },
            onStaffProfileClick = onNavigateToStaffProfile,
            isStaffOrdersSelected = false,
            isProductsSelected = true,
            isStaffProfileSelected = false
        )
    }
}

@Composable
fun ProductsHeader(
    viewModel: ProductsViewModel,
    uiState: ProductsUiState
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category dropdown
            DropdownSelection(viewModel, uiState)

            // Low stock toggle and availability indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Low stock toggle button
                Button(
                    onClick = { viewModel.updateItemAvailability() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.showLowStockOnly) Color(0xFFD32F2F) else Color.Gray
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Low Stock",
                        fontFamily = colfiFont,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFFF43636), shape = androidx.compose.foundation.shape.CircleShape)
                    )
                    Text(
                        text = "Sold Out",
                        fontFamily = colfiFont,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFF4CAF50), shape = androidx.compose.foundation.shape.CircleShape)
                    )
                    Text(
                        text = "Available",
                        fontFamily = colfiFont,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun DropdownSelection(viewModel: ProductsViewModel, uiState: ProductsUiState) {
    val categories = listOf("All", "Coffee", "Non-coffee", "Tea", "Add On")
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = LightBrown2
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.width(120.dp)
        ) {
            Text(
                text = "${uiState.selectedCategory} ▼",
                fontFamily = colfiFont,
                fontSize = 14.sp,
                color = Color.Black
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = category,
                            fontFamily = colfiFont,
                            color = if (category == uiState.selectedCategory) LightBrown2 else DarkBrown1
                        )
                    },
                    onClick = {
                        viewModel.selectCategory(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ProductList(
    products: List<MenuItem>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(products) { item ->
            ProductItemCard(
                menuItem = item
            )
        }
    }
}

@Composable
fun ProductItemCard(
    menuItem: MenuItem,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightCream2),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product image
            Image(
                painter = painterResource(
                    id = if (menuItem.imageName.isNotEmpty()) {
                        menuItem.imageResId
                    } else {
                        // Fallback to category-based image or default
                        DrawableMapper.getDrawableForImageName(menuItem.category)
                    }
                ),
                contentDescription = menuItem.name,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            // Product name
            Text(
                text = menuItem.name,
                fontFamily = colfiFont,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            // Availability indicator (visual only)
            Switch(
                checked = menuItem.availability,
                onCheckedChange = { }, // Disabled - just for visual indication
                enabled = false, // Makes it non-interactive
                colors = SwitchDefaults.colors(
                    disabledCheckedThumbColor = Color.White,
                    disabledCheckedTrackColor = Color(0xFF4CAF50),
                    disabledUncheckedThumbColor = Color.White,
                    disabledUncheckedTrackColor = Color(0xFFF43636)
                )
            )
        }
    }
}