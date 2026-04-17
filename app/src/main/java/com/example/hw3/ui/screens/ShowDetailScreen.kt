package com.example.hw3.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.hw3.model.Show
import com.example.hw3.ui.ShowDetailUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDetailScreen(
    uiState: ShowDetailUiState,
    onBackPressed: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.show != null) uiState.show.name
                        else "Детали"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ошибка: ${uiState.error}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onRetry) {
                            Text("Повторить")
                        }
                    }
                }
                uiState.show != null -> {
                    ShowDetailContent(show = uiState.show)
                }
                else -> {
                    Text("Нет данных")
                }
            }
        }
    }
}

@Composable
private fun ShowDetailContent(show: Show) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row {
            AsyncImage(
                model = show.imageUrl,
                contentDescription = show.name,
                modifier = Modifier.size(150.dp, 220.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Language: ${show.language}")
                Text("Genres: ${show.genres.joinToString(", ")}")
                Text("Rating: ${show.rating ?: "N/A"}")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Summary", fontWeight = FontWeight.Bold)
        Text(show.summary ?: "No summary available")
    }
}