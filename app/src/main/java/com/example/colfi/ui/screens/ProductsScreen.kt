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
import androidx.compose.material.icons.filled.ArrowDropDown
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
            // Header matching Figma design - FIXED alignment
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Product Management",
                    fontFamily = colfiFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "— COLFi —",
                    fontFamily = colfiFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // Category dropdown and availability indicator
            ProductsHeader(viewModel, uiState)

            // Product list
            ProductList(
                products = uiState.filteredItems,
                onToggleAvailability = { item -> viewModel.toggleProductAvailability(item) }
            )
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
            // Category dropdown - FIXED to match reference image
            DropdownSelection(viewModel, uiState)

            // Availability indicators - FIXED layout
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sold Out indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFF43636), shape = androidx.compose.foundation.shape.CircleShape)
                    )
                    Text(
                        text = "Sold Out",
                        fontFamily = colfiFont,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }

                // Available indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
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

        // Low Stock toggle button - MOVED to separate row to match reference
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(
                onClick = { viewModel.toggleLowStockFilter() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.showLowStockOnly) Color(0xFFD32F2F) else LightBrown2
                ),
                shape = RoundedCornerShape(20.dp), // More rounded like reference
                modifier = Modifier.width(100.dp)
            ) {
                Text(
                    text = "Low Stock",
                    fontFamily = colfiFont,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun DropdownSelection(viewModel: ProductsViewModel, uiState: ProductsUiState) {
    val categories = listOf("All", "Coffee", "Non-coffee", "Tea", "Add On")
    var expanded by remember { mutableStateOf(false) }

    Box {
        // Custom dropdown design to match reference image
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = uiState.selectedCategory,
                fontFamily = colfiFont,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Dropdown",
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
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
                            color = if (category == uiState.selectedCategory) LightBrown2 else DarkBrown1,
                            fontWeight = if (category == uiState.selectedCategory) FontWeight.Bold else FontWeight.Normal
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
    products: List<MenuItem>,
    onToggleAvailability: (MenuItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(products) { item ->
            ProductItemCard(
                menuItem = item,
                onToggleAvailability = { onToggleAvailability(item) }
            )
        }
    }
}

@Composable
fun ProductItemCard(
    menuItem: MenuItem,
    onToggleAvailability: (MenuItem) -> Unit
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
            horizontalArrangement = Arrangement.SpaceBetween,
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
                    .size(60.dp) // Smaller image to match reference
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            // Product name - centered
            Text(
                text = menuItem.name,
                fontFamily = colfiFont,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            // Interactive availability toggle - FIXED
            Switch(
                checked = menuItem.availability,
                onCheckedChange = { onToggleAvailability(menuItem) },
                enabled = true, // Now interactive
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF4CAF50),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFF9E9E9E)
                )
            )
        }
    }
}