package com.example.labelwise.features.recent

import app.cash.turbine.test
import com.example.core.result.AppError
import com.example.core.result.AppResult
import com.example.domain.usecase.ObserveRecentProductsUseCase
import com.example.labelwise.FakeProductRepository
import com.example.labelwise.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecentProductsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `flatMapLatest cancels previous refresh when new fetch triggered`() = runTest {
        val repo = FakeProductRepository()
        val observe = ObserveRecentProductsUseCase(repo)

        val vm = RecentProductsViewModel(
            observeRecent = observe,
            repo = repo
        )

        repo.gate("111")
        repo.gate("222")

        vm.onEvent(RecentEvent.BarcodeChanged("111"))
        vm.onEvent(RecentEvent.SearchByBarcode)

        vm.onEvent(RecentEvent.BarcodeChanged("222"))
        vm.onEvent(RecentEvent.SearchByBarcode)

        repo.release("222")

        advanceUntilIdle()

        assertTrue("Expected barcode 111 refresh to be cancelled", repo.cancelledCalls.contains("111"))
        assertTrue(repo.refreshCalls.get() >= 2)
    }

    @Test
    fun `retry retries on Network errors and succeeds on third attempt`() = runTest {
        val repo = FakeProductRepository()
        val observe = ObserveRecentProductsUseCase(repo)

        val vm = RecentProductsViewModel(
            observeRecent = observe,
            repo = repo
        )

        repo.script(
            "999",
            listOf(
                AppResult.Error(AppError.Network),
                AppResult.Error(AppError.Network),
                AppResult.Success(Unit)
            )
        )

        vm.effectFlow.test {
            vm.onEvent(RecentEvent.BarcodeChanged("999"))
            vm.onEvent(RecentEvent.SearchByBarcode)

            advanceUntilIdle()
            expectNoEvents()
        }

        assertEquals(3, repo.refreshCalls.get())
    }

    @Test
    fun `emits Snackbar effect on final failure`() = runTest {
        val repo = FakeProductRepository()
        val observe = ObserveRecentProductsUseCase(repo)

        val vm = RecentProductsViewModel(
            observeRecent = observe,
            repo = repo
        )

        repo.script(
            "404",
            listOf(
                AppResult.Error(AppError.Network),
                AppResult.Error(AppError.Network),
                AppResult.Error(AppError.Network)
            )
        )

        vm.effectFlow.test {
            vm.onEvent(RecentEvent.BarcodeChanged("404"))
            vm.onEvent(RecentEvent.SearchByBarcode)

            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is RecentEffect.Snackbar)
        }

        assertEquals(3, repo.refreshCalls.get())
    }
}