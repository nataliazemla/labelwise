package com.example.labelwise.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.labelwise.features.recent.RecentProductsRoute

@Composable
fun AppNav(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Destinations.RECENT
    ) {
        composable(Destinations.RECENT) {
            RecentProductsRoute(
                onNavigateToDetails = { barcode ->
                    // todo
                }
            )
        }
    }
}