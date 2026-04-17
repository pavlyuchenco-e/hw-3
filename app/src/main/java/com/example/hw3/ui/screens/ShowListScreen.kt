package com.example.hw3.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hw3.model.Show
import com.example.hw3.ui.ShowListUiState
import com.example.hw3.ui.widgets.ShowCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowListScreen(
    searchQuery: String,
    uiState: ShowListUiState,
    onSearchChange: (String) -> Unit,
    onShowClick: (Show) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Поиск сериалов") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Название сериала") },
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                ShowListUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ShowListUiState.Success -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.shows, key = { it.id }) { show ->
                            ShowCard(show = show, onClick = { onShowClick(show) })
                        }
                    }
                }
                is ShowListUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ошибка: ${uiState.message}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { onSearchChange(uiState.searchQuery) }) {
                            Text("Повторить")
                        }
                    }
                }
                ShowListUiState.EmptyQuery -> {
                    Text("Введите название сериала")
                }
                ShowListUiState.NoResults -> {
                    Text("Ничего не найдено")
                }
            }
        }
    }
}