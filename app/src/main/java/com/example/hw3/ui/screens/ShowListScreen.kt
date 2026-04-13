package com.example.hw3.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hw3.ui.ShowListUiState
import com.example.hw3.ui.widgets.ShowCard
import com.example.hw3.model.Show

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowListScreen(
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
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Название сериала") },
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            uiState.errorMessage?.let {
                Text("Ошибка: $it")
                Spacer(modifier = Modifier.height(8.dp))
            }

            when {
                uiState.isLoading -> {
                    Text("Загрузка...")
                }
                !uiState.hasSearched -> {
                    Text("Введите название сериала")
                }
                uiState.showList.isEmpty() -> {
                    Text("Ничего не найдено")
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.showList,
                            key = { it.id }
                        ) { show ->
                            ShowCard(
                                show = show,
                                onClick = { onShowClick(show) }
                            )
                        }
                    }
                }
            }
        }
    }
}