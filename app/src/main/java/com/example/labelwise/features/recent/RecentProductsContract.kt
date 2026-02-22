package com.example.labelwise.features.recent

import com.example.core.udf.UiEffect
import com.example.core.udf.UiEvent
import com.example.core.udf.UiState
import com.example.domain.model.Product

sealed interface RecentEvent : UiEvent {
    data class Refresh(val barcode: String) : RecentEvent
    data object Retry : RecentEvent
    data class BarcodeChanged(val value: String) : RecentEvent
    data object SearchByBarcode : RecentEvent
}

data class RecentState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val barcodeInput: String = "",
    val items: List<Product> = emptyList(),
    val error: String? = null
) : UiState

sealed interface RecentEffect : UiEffect {
    data class Toast(val message: String) : RecentEffect
    data class Snackbar(val message: String) : RecentEffect
}