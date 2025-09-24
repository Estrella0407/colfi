// MenuScreen.kt
package com.example.colfi.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.colfi.DrawableMapper
import com.example.colfi.data.model.CartItem
import com.example.colfi.data.model.MenuItem
import com.example.colfi.data.repository.CartRepository
import com.example.colfi.ui.theme.LightBrown2
import com.example.colfi.ui.theme.LightCream1
import com.example.colfi.ui.theme.colfiFont
import com.example.colfi.ui.viewmodel.CartViewModel
import com.example.colfi.ui.viewmodel.CartViewModelFactory
import com.example.colfi.ui.viewmodel.MenuViewModel

@Composable
fun MenuScreen(
    userName: String,
    menuViewModel: MenuViewModel,
    cartViewModel: CartViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToCustomerProfile: () -> Unit,
    onNavigateToCart: () -> Unit,
    modifier: Modifier = Modifier,
    cartRespository: CartRepository
) {
    val uiState by menuViewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedMenuItemForPopup by remember { mutableStateOf<MenuItem?>(null) }
    val cartViewModel: CartViewModel = viewModel(factory = CartViewModelFactory(cartRespository))

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
        ) {
            MenuHeader()

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Left side - Categories
                Column(
                    modifier = Modifier
                        .width(80.dp)
                        .fillMaxHeight()
                        .background(Color.White)
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    uiState.categories.forEach { category ->
                        MenuCategory(
                            category = menuViewModel.getCategoryDisplayName(category),
                            categoryName = category,
                            isSelected = uiState.selectedCategory == category,
                            onCategorySelected = { menuViewModel.selectCategory(it) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Divider (use simple Box if you don't have VerticalDivider)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(Color.Gray.copy(alpha = 0.3f))
                )

                // Right side - Menu items
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
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
                                onItemDetailClick = { item ->
                                    println("hello world")
                                },
                                onAddToCartClick = { item ->
                                    selectedMenuItemForPopup = item
                                    showDialog = true
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
                .padding(top = 8.dp),
            onMenuClick = { /* Already on menu */ },
            onOrdersClick = onNavigateToOrders,
            onHomeClick = onNavigateToHome,
            onCustomerProfileClick = onNavigateToCustomerProfile,
            isHomeSelected = false,
            isMenuSelected = true,
            isOrdersSelected = false,
            isCustomerProfileSelected = false
        )

        // Popup -> returns a CartItem via onProceedToCart
        if (showDialog && selectedMenuItemForPopup != null) {
            ItemSelectionPopUp(
                menuItem = selectedMenuItemForPopup!!,
                onDismiss = {
                    showDialog = false
                    selectedMenuItemForPopup = null
                },
                onProceedToCart = { cartItem ->
                    cartViewModel.addToCart(cartItem)
                    println("Added ${cartItem.menuItem.name} (Options: ${cartItem.options}, Qty: ${cartItem.quantity}) to cart")
                    showDialog = false
                    selectedMenuItemForPopup = null
                }
            )
        }
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
            HorizontalDivider(
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
    onItemDetailClick: (MenuItem) -> Unit,
    onAddToCartClick: (MenuItem) -> Unit
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
                onItemDetailClick = onItemDetailClick,
                onAddToCartClick = onAddToCartClick
            )
        }
    }
}

@Composable
fun MenuItemCard(
    menuItem: MenuItem,
    onItemDetailClick: (MenuItem) -> Unit,
    onAddToCartClick: (MenuItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemDetailClick(menuItem) },
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
            // Image from drawable resource
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

                    // Add to cart button
                    Button(
                        onClick = { onAddToCartClick(menuItem) },
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

@Composable
fun ItemSelectionPopUp(
    menuItem: MenuItem,
    onDismiss: () -> Unit,
    onProceedToCart: (CartItem) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    var selectedTemperature by remember { mutableStateOf<String?>(null) }
    var selectedSugarLevel by remember { mutableStateOf<String?>(null) }

    val isTemperatureApplicable = menuItem.category in listOf("Coffee", "Tea")
    val isSugarLevelApplicable = menuItem.category in listOf("Coffee", "Tea", "Beverages")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                menuItem.name,
                fontFamily = colfiFont,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.Start) {
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

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    menuItem.description,
                    fontFamily = colfiFont,
                    fontSize = 14.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (isTemperatureApplicable) {
                    Text("Select Temperature:", fontFamily = colfiFont, fontWeight = FontWeight.SemiBold)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("Hot", "Ice").forEach { temp ->
                            Button(
                                onClick = { selectedTemperature = temp },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedTemperature == temp)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selectedTemperature == temp)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) { Text(temp) }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (isSugarLevelApplicable) {
                    Text("Sugar Level:", fontFamily = colfiFont, fontWeight = FontWeight.SemiBold)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("0%", "50%", "100%").forEach { sugar ->
                            Button(
                                onClick = { selectedSugarLevel = sugar },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedSugarLevel == sugar)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selectedSugarLevel == sugar)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) { Text(sugar) }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text("Quantity:", fontFamily = colfiFont, fontWeight = FontWeight.SemiBold)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { if (quantity > 1) quantity-- },
                        shape = CircleShape
                    ) { Text("-") }

                    Text(
                        text = quantity.toString(),
                        fontSize = 18.sp,
                        fontFamily = colfiFont,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    OutlinedButton(
                        onClick = { quantity++ },
                        shape = CircleShape
                    ) { Text("+") }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Total: RM ${String.format("%.2f", menuItem.price * quantity)}",
                    fontFamily = colfiFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isTemperatureApplicable && selectedTemperature == null) {
                        // required selection
                        println("Please select temperature")
                        return@Button
                    }
                    // Build CartItem and return it
                    val cartItem = CartItem(
                        menuItem = menuItem,
                        selectedTemperature = selectedTemperature,
                        selectedSugarLevel = selectedSugarLevel,
                        quantity = quantity
                    )
                    onProceedToCart(cartItem)
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(12.dp)
            ) { Text("Add to Cart", fontSize = 16.sp) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cancel") }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}