// MenuViewModel.kt
package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.repository.MenuRepository
import com.example.colfi.ui.state.MenuUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MenuViewModel(
    private val menuRepository: MenuRepository = MenuRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        initialize()
    }

    private fun initialize() {
        _uiState.value = _uiState.value.copy(
            categories = menuRepository.getCategories()
        )
        loadMenuItems("coffee")
    }

    fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        loadMenuItems(category)
    }

    private fun loadMenuItems(category: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")

        viewModelScope.launch {
            menuRepository.getMenuItemsByCategory(category)
                .onSuccess { items ->
                    _uiState.value = _uiState.value.copy(
                        menuItems = items,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        menuItems = emptyList(),
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to load menu items"
                    )
                }
        }
    }

    fun getCategoryDisplayName(category: String): String {
        return menuRepository.getCategoryDisplayName(category)
    }
}
