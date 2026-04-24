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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDetailScreen(
    uiState: ShowDetailUiState,
    onBackPressed: () -> Unit,
    onRetry: () -> Unit,
    onToggleFavourite: (Show) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (uiState) {
                            is ShowDetailUiState.Success -> uiState.show.name
                            else -> "Детали"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is ShowDetailUiState.Success) {
                        IconButton(onClick = { onToggleFavourite(uiState.show) }) {
                            Icon(
                                imageVector = if (uiState.show.isFavourite) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                                contentDescription = "Избранное"
                            )
                        }
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
            when (uiState) {
                ShowDetailUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is ShowDetailUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ошибка: ${uiState.message}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onRetry) {
                            Text("Повторить")
                        }
                    }
                }
                is ShowDetailUiState.Success -> {
                    ShowDetailContent(show = uiState.show)
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = show.imageUrl,
                contentDescription = show.name,
                modifier = Modifier
                    .width(120.dp)
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = show.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Language: ${show.language}")
                Text("Genres: ${show.genres.joinToString(", ")}")
                Text("Rating: ${show.rating?.toString() ?: "N/A"}")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Summary", fontWeight = FontWeight.Bold)
        Text(
            text = show.summary ?: "No summary available",
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}