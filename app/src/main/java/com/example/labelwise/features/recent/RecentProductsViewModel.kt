package com.example.labelwise.features.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.result.AppError
import com.example.core.result.AppResult
import com.example.domain.repo.ProductRepository
import com.example.domain.usecase.ObserveRecentProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch

@HiltViewModel
class RecentProductsViewModel @Inject constructor(
    observeRecent: ObserveRecentProductsUseCase,
    private val repo: ProductRepository
) : ViewModel() {

    private val effects = Channel<RecentEffect>(Channel.BUFFERED)
    val effectFlow = effects.receiveAsFlow()

    private val barcodeInput = MutableStateFlow("")
    private val fetchRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val isRefreshing = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    private val recentItems = observeRecent()

    val state: StateFlow<RecentState> =
        combine(barcodeInput, isRefreshing, recentItems, error) { input, refreshing, items, err ->
            RecentState(
                barcodeInput = input,
                isRefreshing = refreshing,
                items = items,
                error = err
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecentState())

    init {
        viewModelScope.launch {
            refreshResult.collect { res ->
                isRefreshing.value = false
                when (res) {
                    is AppResult.Success -> Unit
                    is AppResult.Error -> effects.trySend(RecentEffect.Snackbar("Refresh failed: ${res.error}"))
                }
            }
        }
    }

    fun onEvent(event: RecentEvent) {
        when (event) {
            is RecentEvent.BarcodeChanged -> barcodeInput.value = event.value
            RecentEvent.SearchByBarcode -> triggerFetch()
            is RecentEvent.Refresh -> refresh()

            else -> {}
        }
    }

    private fun triggerFetch() {
        isRefreshing.value = true
        fetchRequests.tryEmit(Unit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val refreshResult: SharedFlow<AppResult<Unit>> =
        fetchRequests
            .map { barcodeInput.value.trim() }
            .filter { it.isNotBlank() }
            .distinctUntilChanged()
            .flatMapLatest { barcode -> refreshWithRetry(barcode) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), replay = 0)


    @OptIn(ExperimentalCoroutinesApi::class)
    private fun refreshWithRetry(barcode: String): Flow<AppResult<Unit>> = flow {
        emit(repo.refreshByBarcode(barcode))
    }.transformLatest { result ->
        var attempt = 0
        var current = result
        while (current is AppResult.Error &&
            (current.error is AppError.Network || current.error is AppError.Timeout) &&
            attempt < 2
        ) {
            attempt++
            delay(300L * (1 shl (attempt - 1)))
            current = repo.refreshByBarcode(barcode)
        }
        emit(current)
    }


    private fun refresh() {
        val barcode = barcodeInput.value.trim()
        if (barcode.isBlank()) {
            effects.trySend(RecentEffect.Snackbar("Enter barcode"))
            return
        }

        viewModelScope.launch {
            isRefreshing.value = true
            error.value = null
            when (val res = repo.refreshByBarcode(barcode)) {
                is AppResult.Success -> {
                    // UI zaktualizuje się sama z DB (observeRecent)
                }

                is AppResult.Error -> {
                    error.value = res.error.toString()
                    effects.trySend(RecentEffect.Snackbar("Refresh failed: ${res.error}"))
                }
            }
            isRefreshing.value = false
        }
    }
}