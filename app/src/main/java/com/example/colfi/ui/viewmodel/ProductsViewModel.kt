// ProductsViewModel.kt
package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.model.MenuItem
import com.example.colfi.data.repository.MenuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.colfi.ui.state.ProductsUiState

class ProductsViewModel(
    private val menuRepository: MenuRepository = MenuRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    init {
        loadMenuItems()
        loadLowStockItems()
    }

    fun selectCategory(category: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedCategory = category,
                filteredItems = filterItemsByCategory(category, _uiState.value.products)
            )

            // If a specific category is selected, reload items for that category
            if (category != "All") {
                loadMenuItemsForCategory(category)
            }
        }
    }

    fun loadMenuItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val result = if (_uiState.value.selectedCategory == "All") {
                    menuRepository.getAllMenuItems()
                } else {
                    val categoryKey = getCategoryKey(_uiState.value.selectedCategory)
                    menuRepository.getMenuItemsByCategory(categoryKey)
                }

                result.fold(
                    onSuccess = { items ->
                        _uiState.value = _uiState.value.copy(
                            products = items,
                            filteredItems = filterItemsByCategory(_uiState.value.selectedCategory, items),
                            isLoading = false
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to load menu items"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun loadMenuItemsForCategory(category: String) {
        if (category == "All") {
            loadMenuItems()
            return
        }

        viewModelScope.launch {
            val categoryKey = getCategoryKey(category)
            menuRepository.getMenuItemsByCategory(categoryKey).fold(
                onSuccess = { items ->
                    // Update the specific items in the list
                    val currentItems = _uiState.value.products.toMutableList()
                    // Remove old items from this category
                    currentItems.removeAll { it.category == categoryKey }
                    // Add new items
                    currentItems.addAll(items)

                    _uiState.value = _uiState.value.copy(
                        products = currentItems,
                        filteredItems = filterItemsByCategory(_uiState.value.selectedCategory, currentItems)
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "Failed to refresh category items"
                    )
                }
            )
        }
    }

    fun loadLowStockItems() {
        viewModelScope.launch {
            menuRepository.getLowStockItems().fold(
                onSuccess = { items ->
                    _uiState.value = _uiState.value.copy(lowStockItems = items)
                },
                onFailure = {
                    // Handle silently or log error
                }
            )
        }
    }

    fun updateItemAvailability() {
        val showLowStock = !_uiState.value.showLowStockOnly
        _uiState.value = _uiState.value.copy(
            showLowStockOnly = showLowStock,
            filteredItems = if (showLowStock) {
                _uiState.value.lowStockItems
            } else {
                filterItemsByCategory(_uiState.value.selectedCategory, _uiState.value.products)
            }
        )
    }

    fun updateItemQuantity(item: MenuItem, newQuantity: Int) {
        viewModelScope.launch {
            menuRepository.updateItemQuantity(item.category, item.id, newQuantity).fold(
                onSuccess = {
                    // Update the specific item in the current list
                    refreshItemInList(item.id, item.category)
                    loadLowStockItems()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "Failed to update quantity"
                    )
                }
            )
        }
    }

    fun increaseItemQuantity(item: MenuItem, amount: Int = 1) {
        viewModelScope.launch {
            menuRepository.increaseItemQuantity(item.category, item.id, amount).fold(
                onSuccess = {
                    refreshItemInList(item.id, item.category)
                    loadLowStockItems()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "Failed to increase quantity"
                    )
                }
            )
        }
    }

    fun decreaseItemQuantity(item: MenuItem, amount: Int = 1) {
        viewModelScope.launch {
            menuRepository.decreaseItemQuantity(item.category, item.id, amount).fold(
                onSuccess = {
                    refreshItemInList(item.id, item.category)
                    loadLowStockItems()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "Failed to decrease quantity"
                    )
                }
            )
        }
    }

    // Refresh a specific item after quantity changes
    private fun refreshItemInList(itemId: String, category: String) {
        viewModelScope.launch {
            menuRepository.getMenuItemById(category, itemId).fold(
                onSuccess = { updatedItem ->
                    if (updatedItem != null) {
                        val currentItems = _uiState.value.products.toMutableList()
                        val index = currentItems.indexOfFirst { it.id == itemId }
                        if (index >= 0) {
                            currentItems[index] = updatedItem
                            _uiState.value = _uiState.value.copy(
                                products = currentItems,
                                filteredItems = filterItemsByCategory(_uiState.value.selectedCategory, currentItems)
                            )
                        }
                    }
                },
                onFailure = {
                    // Fallback to full reload
                    loadMenuItems()
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun filterItemsByCategory(category: String, items: List<MenuItem>): List<MenuItem> {
        return if (category == "All") {
            items
        } else {
            val categoryKey = getCategoryKey(category)
            items.filter { it.category.equals(categoryKey, ignoreCase = true) }
        }
    }

    private fun getCategoryKey(displayName: String): String {
        return when (displayName) {
            "Coffee" -> "coffee"
            "Non-coffee" -> "non-coffee"
            "Tea" -> "tea"
            "Add On" -> "add-on"
            else -> displayName.lowercase()
        }
    }
}