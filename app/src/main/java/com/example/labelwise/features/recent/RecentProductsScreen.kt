@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.labelwise.features.recent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RecentProductsRoute(
    onNavigateToDetails: (barcode: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecentProductsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // one-shot effects (snackbar/toast/navigation)
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.effectFlow.collectLatest { effect ->
            when (effect) {
                is RecentEffect.Toast ->
                    snackbarHostState.showSnackbar(effect.message)
                is RecentEffect.Snackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    RecentProductScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onItemClick = { onNavigateToDetails(it.barcode) },
        modifier = modifier
    )
}

@Composable
private fun RecentProductScreen(
    state: RecentState,
    snackbarHostState: SnackbarHostState,
    onEvent: (RecentEvent) -> Unit,
    onItemClick: (com.example.domain.model.Product) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("LabelWise") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.barcodeInput,
                onValueChange = { onEvent(RecentEvent.BarcodeChanged(it)) },
                label = { Text("Barcode") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { onEvent(RecentEvent.SearchByBarcode) },
                enabled = !state.isRefreshing,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isRefreshing) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text("Fetch from Open Food Facts")
            }

            if (state.error != null) {
                Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
            }

            RecentProductsList(
                items = state.items,
                onItemClick = { /* later: navigate details */ },
                modifier = Modifier.weight(1f)
            )
        }
    }
//        Box(
//            modifier = modifier
//                .fillMaxSize()
//                .padding(padding)
//        ) {
//            when {
//                state.isLoading -> {
//                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
//                }
//                state.error != null -> {
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        Text("Error: ${state.error}")
//                        Spacer(Modifier.height(8.dp))
//                        Button(onClick = { onEvent(RecentEvent.Retry) }) {
//                            Text("Retry")
//                        }
//                    }
//                }
//                state.items.isEmpty() -> {
//                    Text(
//                        "No recent products yet. Search by barcode to start.",
//                        modifier = Modifier.padding(16.dp)
//                    )
//                }
//                else -> {
//                    RecentProductsList(
//                        items = state.items,
//                        onItemClick = onItemClick
//                    )
//                }
//            }
//        }
//    }
}