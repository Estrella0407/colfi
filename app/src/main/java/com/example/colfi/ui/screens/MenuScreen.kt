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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colfi.DrawableMapper
import com.example.colfi.R
import com.example.colfi.data.model.CartItem
import com.example.colfi.data.model.MenuItem
import com.example.colfi.data.repository.CartRepository
import com.example.colfi.ui.state.MenuUiState
import com.example.colfi.ui.theme.BackgroundColor
import com.example.colfi.ui.theme.DarkBrown1
import com.example.colfi.ui.theme.LightBrown2
import com.example.colfi.ui.theme.LightCream1
import com.example.colfi.ui.theme.colfiFont
import com.example.colfi.ui.viewmodel.CartViewModel
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
    cartRepository: CartRepository
) {
    val uiState by menuViewModel.uiState.collectAsState()
    val cartUiState by cartViewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var selectedMenuItemForPopup by remember { mutableStateOf<MenuItem?>(null) }
    var lastAddedItem by remember { mutableStateOf<CartItem?>(null) }

    // Get screen configuration for responsive layout
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp >= configuration.screenHeightDp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(LightCream1)
    ) {
        if (isLandscape) {
            // LANDSCAPE LAYOUT - Figma Design
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                // Left Sidebar Navigation
                LandscapeLeftSidebar(
                    onHomeClick = onNavigateToHome,
                    onMenuClick = { /* Already on menu */ },
                    onOrdersClick = onNavigateToOrders,
                    onCustomerProfileClick = onNavigateToCustomerProfile,
                    isHomeSelected = false,
                    isMenuSelected = true,
                    isOrdersSelected = false,
                    isCustomerProfileSelected = false
                )

                // Main Content Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // Header with COLFi title
                    LandscapeHeader()

                    // Menu content
                    LandscapeMenuContent(
                        uiState = uiState,
                        menuViewModel = menuViewModel,
                        screenWidth = configuration.screenWidthDp.dp,
                        onItemDetailClick = { item ->
                            println("Item detail clicked: ${item.name}")
                        },
                        onAddToCartClick = { item ->
                            selectedMenuItemForPopup = item
                            showDialog = true
                        }
                    )
                }
            }
        } else {
            // PORTRAIT LAYOUT (unchanged)
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                PortraitMenuHeader()

                PortraitMenuLayout(
                    uiState = uiState,
                    menuViewModel = menuViewModel,
                    onItemDetailClick = { item ->
                        println("Item detail clicked: ${item.name}")
                    },
                    onAddToCartClick = { item ->
                        selectedMenuItemForPopup = item
                        showDialog = true
                    }
                )
            }
        }

        // Floating Cart Button
        if (cartUiState.itemCount > 0) {
            FloatingActionButton(
                onClick = onNavigateToCart,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 16.dp,
                        bottom = if (isLandscape) 16.dp else 112.dp
                    )
                    .navigationBarsPadding(), // ← Add this for extra safety
                containerColor = Color(0xFFD2B48C),
                contentColor = Color.Black
            ) {
                BadgedBox(
                    badge = {
                        if (cartUiState.itemCount > 0) {
                            Badge(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = cartUiState.itemCount.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Cart",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Bottom Navigation Bar - Only show in portrait mode
        if (!isLandscape) {
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
        }

        // Popup dialogs (unchanged)
        if (showDialog && selectedMenuItemForPopup != null) {
            ItemSelectionPopUp(
                menuItem = selectedMenuItemForPopup!!,
                onDismiss = {
                    showDialog = false
                    selectedMenuItemForPopup = null
                },
                onProceedToCart = { cartItem ->
                    cartViewModel.addToCart(cartItem)
                    lastAddedItem = cartItem
                    showDialog = false
                    selectedMenuItemForPopup = null
                    showConfirmationDialog = true
                }
            )
        }

        if (showConfirmationDialog && lastAddedItem != null) {
            ConfirmationDialog(
                cartItem = lastAddedItem!!,
                onDismiss = {
                    showConfirmationDialog = false
                    lastAddedItem = null
                },
                onContinueShopping = {
                    showConfirmationDialog = false
                    lastAddedItem = null
                },
                onGoToCart = {
                    showConfirmationDialog = false
                    lastAddedItem = null
                    onNavigateToCart()
                }
            )
        }

        if (cartUiState.errorMessage.isNotEmpty()) {
            Text(
                text = "Cart Error: ${cartUiState.errorMessage}",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .background(Color.Red.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                    .padding(8.dp),
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}



@Composable
fun LandscapeHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .background(LightCream1)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Menu",
            fontFamily = colfiFont,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "— COLFi —",
            fontFamily = colfiFont,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun LandscapeMenuContent(
    uiState: MenuUiState,
    menuViewModel: MenuViewModel,
    screenWidth: Dp,
    onItemDetailClick: (MenuItem) -> Unit,
    onAddToCartClick: (MenuItem) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // Category tabs (horizontal)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LightCream1)
        ) {
            Column {
                // Category selection bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    uiState.categories.forEach { category ->
                        LandscapeMenuCategory(
                            category = menuViewModel.getCategoryDisplayName(category),
                            categoryName = category,
                            isSelected = uiState.selectedCategory == category,
                            onCategorySelected = { menuViewModel.selectCategory(it) }
                        )
                    }
                }

                HorizontalDivider(
                    color = Color.Gray.copy(alpha = 0.3f),
                    thickness = 1.dp
                )

                // Menu items grid
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
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
                            // Calculate adaptive columns based on screen width
                            val columns = when {
                                screenWidth < 600.dp -> 2
                                screenWidth < 900.dp -> 3
                                else -> 4
                            }

                            MenuItemsGrid(
                                menuItems = uiState.menuItems,
                                columns = columns,
                                onItemDetailClick = onItemDetailClick,
                                onAddToCartClick = onAddToCartClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LandscapeMenuCategory(
    category: String,
    categoryName: String,
    isSelected: Boolean,
    onCategorySelected: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onCategorySelected(categoryName) }
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = category,
            fontFamily = colfiFont,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.Black else Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (isSelected) {
            HorizontalDivider(
                modifier = Modifier.width(80.dp),
                color = Color(0xFFD2B48C),
                thickness = 2.dp
            )
        }
    }

}

@Composable
fun PortraitMenuHeader() {
    Column {
        // Title row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .background(LightCream1)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Menu",
                fontFamily = colfiFont,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "— COLFi —",
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
fun PortraitMenuLayout(
    uiState: MenuUiState,
    menuViewModel: MenuViewModel,
    onItemDetailClick: (MenuItem) -> Unit,
    onAddToCartClick: (MenuItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // Left side - Categories (original width)
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

        // Divider
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(Color.Gray.copy(alpha = 0.3f))
        )

        // Right side - Menu items in list
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
                        onItemDetailClick = onItemDetailClick,
                        onAddToCartClick = onAddToCartClick
                    )
                }
            }
        }
    }
}

@Composable
fun MenuItemsGrid(
    menuItems: List<MenuItem>,
    columns: Int,
    onItemDetailClick: (MenuItem) -> Unit,
    onAddToCartClick: (MenuItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        items(menuItems) { item ->
            MenuItemCardCompact(
                menuItem = item,
                onItemDetailClick = {
                    if (item.availability) {
                        onItemDetailClick(item)
                    }
                },
                onAddToCartClick = {
                    if (item.availability) {
                        onAddToCartClick(item)
                    }
                }
            )
        }
    }
}

@Composable
fun MenuItemCardCompact(
    menuItem: MenuItem,
    onItemDetailClick: (MenuItem) -> Unit,
    onAddToCartClick: (MenuItem) -> Unit
) {
    val isOutOfStock = !menuItem.availability

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = !isOutOfStock,
                onClick = {
                    if (!isOutOfStock) {
                        onItemDetailClick(menuItem)
                    }
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(

            containerColor = if (isOutOfStock) Color.LightGray.copy(alpha = 0.5f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isOutOfStock) 1.dp else 2.dp
        )
    ) {
        Box {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Image Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp) // Fixed height for compact image
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray.copy(alpha = 0.2f))
                ) {
                    Image(
                        painter = painterResource(
                            id = if (menuItem.imageName.isNotEmpty()) {
                                menuItem.imageResId
                            } else {
                                DrawableMapper.getDrawableForImageName(menuItem.category)
                            }
                        ),
                        contentDescription = menuItem.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,

                        colorFilter = if (isOutOfStock) ColorFilter.tint(
                            Color.Gray,
                            blendMode = BlendMode.Saturation
                        ) else null
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    Text(
                        text = menuItem.name,
                        fontFamily = colfiFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOutOfStock) Color.DarkGray else Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (menuItem.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = menuItem.description,
                            fontFamily = colfiFont,
                            fontSize = 10.sp,
                            color = if (isOutOfStock) Color.Gray else Color.DarkGray,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOutOfStock) Color.Gray else Color(0xFFD2B48C)
                    )

                    Button(
                        onClick = {

                            if (!isOutOfStock) {
                                onAddToCartClick(menuItem)
                            }
                        },
                        enabled = !isOutOfStock,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD2B48C),
                            disabledContainerColor = Color.DarkGray.copy(alpha = 0.3f),
                            contentColor = Color.Black,
                            disabledContentColor = Color.Gray.copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(
                            horizontal = 8.dp,
                            vertical = 2.dp
                        ) // Compact padding
                    ) {
                        Text(
                            text = "Add",
                            fontFamily = colfiFont,
                            fontSize = 8.sp
                        )
                    }
                }
            }


            if (isOutOfStock) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .clip(RoundedCornerShape(12.dp))
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(
                            Color.Red.copy(alpha = 0.9f),
                            RoundedCornerShape(4.dp)
                        ) // Red background for text
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "OUT OF STOCK",
                        color = Color.White,
                        fontFamily = colfiFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
@Composable
fun ConfirmationDialog(
    cartItem: CartItem,
    onDismiss: () -> Unit,
    onContinueShopping: () -> Unit,
    onGoToCart: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = DarkBrown1,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Added to Cart!",
                    fontFamily = colfiFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFFD2B48C)
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = cartItem.menuItem.name,
                    fontFamily = colfiFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                if (cartItem.options.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = cartItem.options,
                        fontFamily = colfiFont,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Quantity:",
                        fontFamily = colfiFont,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${cartItem.quantity}",
                        fontFamily = colfiFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Total:",
                        fontFamily = colfiFont,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "RM ${String.format("%.2f", cartItem.totalPrice)}",
                        fontFamily = colfiFont,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD2B48C)
                    )
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onGoToCart,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD2B48C)
                    ),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Text(
                        "Go to Cart",
                        fontFamily = colfiFont,
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onContinueShopping,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFD2B48C)
                    ),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Text(
                        "Continue Shopping",
                        fontFamily = colfiFont,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        dismissButton = null,
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        items(menuItems) { item ->
            MenuItemCard(
                menuItem = item,
                onItemDetailClick = {
                    // Only allow detail click if item is in stock
                    if (item.availability) {
                        onItemDetailClick(item)
                    }
                },
                onAddToCartClick = {
                    // Only allow add to cart if item is in stock
                    if (item.availability) {
                        onAddToCartClick(item)
                    }
                }
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
    val isOutOfStock = !menuItem.availability

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = !isOutOfStock,
                onClick = { onItemDetailClick(menuItem) }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            // Keep the container slightly dimmed to indicate unavailability
            containerColor = if (isOutOfStock) Color.LightGray.copy(alpha = 0.6f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOutOfStock) 2.dp else 4.dp)
    ) {
        Box { // Use a Box to layer elements
            // Main content of the card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Image
                Box(
                    modifier = Modifier.size(100.dp)
                ) {
                    Image(
                        painter = painterResource(
                            id = if (menuItem.imageName.isNotEmpty()) {
                                menuItem.imageResId
                            } else {
                                // Make sure DrawableMapper.getDrawableForImageName exists and returns a valid @DrawableRes
                                // For example: R.drawable.default_category_icon
                                DrawableMapper.getDrawableForImageName(menuItem.category)
                            }
                        ),
                        contentDescription = menuItem.name,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        // Apply a greyscale filter directly to the image when out of stock
                        colorFilter = if (isOutOfStock) ColorFilter.tint(Color.Gray, blendMode = BlendMode.Saturation) else null
                    )
                }

                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(), // Ensure column takes available height
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = menuItem.name,
                            fontFamily = colfiFont, // Ensure colfiFont is defined
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOutOfStock) Color.DarkGray else Color.Black
                        )

                        if (menuItem.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = menuItem.description,
                                fontFamily = colfiFont,
                                fontSize = 14.sp,
                                color = if (isOutOfStock) Color.Gray else Color.DarkGray,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp)) // Ensure spacing

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
                            color = if (isOutOfStock) Color.Gray else Color(0xFFD2B48C)
                        )

                        Button(
                            onClick = { onAddToCartClick(menuItem) },
                            enabled = !isOutOfStock,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD2B48C), // Normal color
                                disabledContainerColor = Color.Gray.copy(alpha = 0.5f) // Disabled color
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(36.dp), // Slightly increased height for better touch
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                // The button is disabled, so its text doesn't need to change to "Out of Stock"
                                // The visual state of the button (disabled) and the overlay will indicate this.
                                text = "Add",
                                fontFamily = colfiFont,
                                fontSize = 14.sp, // Slightly increased for readability
                                color = Color.Black // Keep text color consistent
                            )
                        }
                    }
                }
            }

            // Out of Stock Overlay and Text - Drawn on top of the Row content
            if (isOutOfStock) {
                // Overlay to dim the entire card content
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.5f)) // Adjust alpha for desired dimness
                        .clip(RoundedCornerShape(12.dp)) // Match card's shape
                )

                // "OUT OF STOCK" Text, centered on top of the overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.Red.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp) // Increased padding
                ) {
                    Text(
                        text = "OUT OF STOCK",
                        color = Color.White,
                        fontFamily = colfiFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp // Slightly larger for prominence
                    )
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
    val isOutOfStock = !menuItem.availability

    if (isOutOfStock) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    "Item Unavailable",
                    fontFamily = colfiFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Red
                )
            },
            text = {
                Text(
                    "${menuItem.name} is currently out of stock. Please check back later.",
                    fontFamily = colfiFont,
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD2B48C))
                ) {
                    Text("OK", fontFamily = colfiFont)
                }
            }
        )
        return
    }
    var quantity by remember { mutableIntStateOf(1) }
    var selectedTemperature by remember { mutableStateOf<String?>(null) }
    var selectedSugarLevel by remember { mutableStateOf<String?>(null) }
    var showValidationError by remember { mutableStateOf(false) }

    val isTemperatureApplicable = menuItem.category.lowercase() in listOf("coffee", "tea")
    val isSugarLevelApplicable = menuItem.category.lowercase() in listOf("coffee", "tea", "non-coffee")
    val scrollState = rememberScrollState()
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
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxHeight(fraction = 0.8f)
            ) {
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
                        CartItem.TEMPERATURE_OPTIONS.forEach { temp ->
                            Button(
                                onClick = {
                                    selectedTemperature = temp
                                    showValidationError = false
                                },
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

                if (isSugarLevelApplicable)
                {
                    Text(
                        "Sugar Level:",
                        fontFamily = colfiFont,
                        fontWeight = FontWeight.SemiBold
                    )

                    val sugarOptions = CartItem.SUGAR_LEVEL_OPTIONS.filter { it.isNotBlank() }

                    // Split into two rows
                    val firstRow = sugarOptions.take(2)   // First 2 options
                    val secondRow = sugarOptions.drop(2)  // Remaining options

                    // First row
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        firstRow.forEach { sugar ->
                            Button(
                                onClick = {
                                    selectedSugarLevel = sugar
                                    showValidationError = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedSugarLevel == sugar)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selectedSugarLevel == sugar)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(sugar, maxLines = 1, softWrap = true)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Second row
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        secondRow.forEach { sugar ->
                            Button(
                                onClick = {
                                    selectedSugarLevel = sugar
                                    showValidationError = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedSugarLevel == sugar)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selectedSugarLevel == sugar)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(sugar, maxLines = 1, softWrap = true)
                            }
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

                // Show validation error if needed
                if (showValidationError) {
                    Text(
                        text = "Please select all required options",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontFamily = colfiFont
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

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
                    // Validate required selections
                    if ((isTemperatureApplicable && selectedTemperature == null) ||
                        (isSugarLevelApplicable && selectedSugarLevel == null)) {
                        showValidationError = true
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