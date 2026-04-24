package com.example.hw3.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hw3.model.Show
import com.example.hw3.ui.FavouritesUiState
import com.example.hw3.ui.widgets.ShowCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesScreen(
    uiState: FavouritesUiState,
    onShowClick: (Int) -> Unit,
    onBackPressed: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(
            title = { Text("Избранное") },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                FavouritesUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is FavouritesUiState.Success -> {
                    if (uiState.shows.isEmpty()) {
                        Text("Нет избранных шоу")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.shows, key = { it.id }) { show ->
                                ShowCard(
                                    show = show,
                                    onClick = { onShowClick(show.id) }
                                )
                            }
                        }
                    }
                }
                is FavouritesUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ошибка: ${uiState.message}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { }) {
                            Text("Повторить")
                        }
                    }
                }
            }
        }
    }
}