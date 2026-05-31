package com.example.hw3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hw3.ui.ShowViewModel
import com.example.hw3.ui.screens.FavouritesScreen
import com.example.hw3.ui.screens.ShowDetailScreen
import com.example.hw3.ui.screens.ShowListScreen
import com.example.hw3.model.Show

@Composable
fun NavGraph(
    startDestination: String = "list",
    viewModel: ShowViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("list") {
            val uiState by viewModel.uiState.collectAsState()
            ShowListScreen(
                searchQuery = viewModel.searchQuery,
                uiState = uiState,
                onSearchChange = viewModel::onSearchQueryChange,
                onShowClick = { show: Show ->
                    navController.navigate("detail/${show.id}")
                },
                onNavigateToFavourites = {
                    navController.navigate("favourites")
                }
            )
        }
        composable("favourites") {
            val favouritesState by viewModel.favouritesUiState.collectAsState()
            LaunchedEffect(Unit) {
                viewModel.loadFavourites()
            }
            FavouritesScreen(
                uiState = favouritesState,
                onShowClick = { showId: Int ->
                    navController.navigate("detail/$showId")
                },
                onBackPressed = { navController.popBackStack() },
                onRetry = { viewModel.loadFavourites() }
            )
        }

        composable(
            route = "detail/{showId}",
            arguments = listOf(navArgument("showId") { type = NavType.IntType })
        ) { backStackEntry ->
            val showId = backStackEntry.arguments?.getInt("showId") ?: return@composable
            LaunchedEffect(showId) {
                viewModel.resetDetailState()
                viewModel.loadShowById(showId)
            }
            val detailState by viewModel.detailUiState
            ShowDetailScreen(
                uiState = detailState,
                onBackPressed = { navController.popBackStack() },
                onRetry = { viewModel.loadShowById(showId) },
                onToggleFavourite = { show ->
                    viewModel.toggleFavourite(show)
                }
            )
        }
    }
}