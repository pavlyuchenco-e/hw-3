package com.example.hw3

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hw3.ui.ShowViewModel
import com.example.hw3.ui.screens.ShowDetailScreen
import com.example.hw3.ui.screens.ShowListScreen

@Composable
fun ShowApp() {
    val navController = rememberNavController()
    val showViewModel: ShowViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "list"
    ) {
        composable("list") {
            ShowListScreen(
                uiState = showViewModel.uiState,
                onSearchChange = showViewModel::onSearchQueryChange,
                onShowClick = { show ->
                    navController.navigate("detail/${show.id}")
                }
            )
        }
        composable(
            route = "detail/{showId}",
            arguments = listOf(navArgument("showId") { type = NavType.IntType })
        ) { backStackEntry ->
            val showId = backStackEntry.arguments?.getInt("showId") ?: return@composable
            val show = showViewModel.getShowById(showId)
            if (show != null) {
                ShowDetailScreen(
                    show = show,
                    onBackPressed = { navController.popBackStack() }
                )
            } else {
                navController.popBackStack()
            }
        }
    }
}