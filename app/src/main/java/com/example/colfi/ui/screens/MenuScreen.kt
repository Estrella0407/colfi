// MenuScreen.kt
package com.example.colfi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.colfi.data.model.MenuItem
import com.example.colfi.ui.theme.LightBrown2
import com.example.colfi.ui.theme.LightCream1
import com.example.colfi.ui.theme.colfiFont
import com.example.colfi.ui.viewmodel.MenuViewModel

@Composable
fun MenuScreen(
    userName: String,
    onNavigateToHome: () -> Unit,
    onNavigateToOrders: () -> Unit,
    viewModel: MenuViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding() // Ensure navigation bar padding is applied
            .background(LightCream1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 56.dp) // Adjust for bottom navigation height
        ) {
            MenuHeader()

            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                // Left side - Categories
                Column(
                    modifier = Modifier
                        .width(120.dp)
                        .fillMaxHeight()
                        .background(Color.White)
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    uiState.categories.forEach { category ->
                        MenuCategory(
                            category = viewModel.getCategoryDisplayName(category),
                            categoryName = category,
                            isSelected = uiState.selectedCategory == category,
                            onCategorySelected = { viewModel.selectCategory(it) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Vertical divider
                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = Color.Gray.copy(alpha = 0.3f)
                )

                // Right side - Menu items
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
                                    text = "Error loading menu",
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

                        uiState.menuItems.isEmpty() -> {
                            Text(
                                text = "No items available in this category",
                                modifier = Modifier.align(Alignment.Center),
                                fontFamily = colfiFont,
                                color = Color.Gray
                            )
                        }

                        else -> {
                            MenuItemsList(
                                menuItems = uiState.menuItems,
                                onItemClick = { item ->
                                    // Handle item click - you can add navigation to item detail here
                                    println("Clicked on: ${item.name}")
                                }
                            )
                        }
                    }
                }
            }
        }

        // Bottom Navigation Bar
        BottomNavigation(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            onMenuClick = { /* Already on menu */ },
            onOrdersClick = onNavigateToOrders,
            onHomeClick = onNavigateToHome,
            isHomeSelected = false,
            isOrdersSelected = false,
            isMenuSelected = true
        )
    }
}

@Composable
fun MenuHeader() {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            // Menu button on the left
            Button(
                onClick = { /* Already on menu */ },
                modifier = Modifier
                    .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                    .width(80.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightBrown2
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Menu",
                    fontFamily = colfiFont,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }

            // COLFi text on the right
            Text(
                text = "— COLFi —",
                modifier = Modifier.padding(end = 16.dp),
                fontFamily = colfiFont,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        // Add a divider after header
        HorizontalDivider(
            color = Color.Gray.copy(alpha = 0.3f),
            thickness = 1.dp
        )
    }
}

@Composable
fun MenuCategory(
    category: String,
    categoryName: String,
    isSelected: Boolean,
    onCategorySelected: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onCategorySelected(categoryName) }
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = category,
            fontFamily = colfiFont,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFFD2B48C) else Color.Black
        )

        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Divider(
                modifier = Modifier.width(60.dp),
                color = Color(0xFFD2B48C),
                thickness = 2.dp
            )
        }
    }
}

@Composable
fun MenuItemsList(
    menuItems: List<MenuItem>,
    onItemClick: (MenuItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(menuItems) { item ->
            MenuItemCard(
                menuItem = item,
                onItemClick = onItemClick
            )
        }
    }
}

@Composable
fun MenuItemCard(
    menuItem: MenuItem,
    onItemClick: (MenuItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(menuItem) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image
            AsyncImage(
                model = if (menuItem.imageURL.isNotEmpty()) menuItem.imageURL else "https://via.placeholder.com/100x100?text=No+Image",
                contentDescription = menuItem.name,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = menuItem.name,
                        fontFamily = colfiFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    if (menuItem.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = menuItem.description,
                            fontFamily = colfiFont,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RM ${String.format("%.2f", menuItem.price)}",
                        fontFamily = colfiFont,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD2B48C)
                    )

                    // Add to cart button (optional)
                    Button(
                        onClick = { onItemClick(menuItem) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD2B48C)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Add",
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