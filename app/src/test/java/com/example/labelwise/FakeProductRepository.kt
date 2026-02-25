package com.example.labelwise

import com.example.core.result.AppResult
import com.example.domain.model.Product
import com.example.domain.repo.ProductRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class FakeProductRepository : ProductRepository {

    private val recent = MutableStateFlow<List<Product>>(emptyList())
    private val scripted = ConcurrentHashMap<String, ArrayDeque<AppResult<Unit>>>()
    private val gates = ConcurrentHashMap<String, CompletableDeferred<Unit>>()

    val cancelledCalls = mutableListOf<String>()
    val refreshCalls = AtomicInteger(0)

    override fun observeRecent(): Flow<List<Product>> = recent

    fun setRecent(items: List<Product>) {
        recent.value = items
    }

    fun script(barcode: String, results: List<AppResult<Unit>>) {
        scripted[barcode] = ArrayDeque(results)
    }

    fun gate(barcode: String) {
        gates[barcode] = CompletableDeferred()
    }

    fun release(barcode: String) {
        gates[barcode]?.complete(Unit)
    }

    override suspend fun refreshByBarcode(barcode: String): AppResult<Unit> {
        refreshCalls.incrementAndGet()

        val gate = gates[barcode]
        if (gate != null) {
            try {
                gate.await()
            } catch (ce: CancellationException) {
                cancelledCalls += barcode
                throw ce
            }
        }

        val queue = scripted[barcode]
        if (queue != null && queue.isNotEmpty()) return queue.removeFirst()

        return AppResult.Success(Unit)
    }
}