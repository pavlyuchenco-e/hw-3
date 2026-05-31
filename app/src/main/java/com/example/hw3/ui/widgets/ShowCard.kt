package com.example.hw3.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.hw3.model.Show

@Composable
fun ShowCard(
    show: Show,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = show.imageUrl,
            contentDescription = show.name,
            modifier = Modifier.size(80.dp, 120.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = show.name,
                fontWeight = FontWeight.Bold
            )
            Text("Language: ${show.language}")
            Text("Genres: ${show.genres.joinToString(", ")}")
            Text("Rating: ${show.rating ?: "N/A"}")
        }
    }
}