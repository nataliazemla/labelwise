package com.example.labelwise.features.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.result.AppResult
import com.example.domain.repo.ProductRepository
import com.example.domain.usecase.ObserveRecentProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class RecentProductsViewModel @Inject constructor(
    observeRecent: ObserveRecentProductsUseCase,
    private val repo: ProductRepository
) : ViewModel() {

    private val effects = Channel<RecentEffect>(Channel.BUFFERED)
    val effectFlow = effects.receiveAsFlow()

    val state: StateFlow<RecentState> =
        observeRecent()
            .map { RecentState(items = it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecentState(isLoading = true))

    fun onEvent(event: RecentEvent) {
        when (event) {
            is RecentEvent.Refresh -> viewModelScope.launch {
                val res = repo.refreshByBarcode(event.barcode)
                if (res is AppResult.Error) {
                    effects.trySend(RecentEffect.Toast("Refresh failed: ${res.error}"))
                }
            }

            else -> {}
        }
    }
}