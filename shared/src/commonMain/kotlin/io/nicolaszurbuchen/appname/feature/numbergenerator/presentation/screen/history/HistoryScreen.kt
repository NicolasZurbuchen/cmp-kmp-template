package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun HistoryScreen(
    state: HistoryUiModel,
    onToggleFavorite: (Long) -> Unit,
    onSyncNowClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = onBackClick) { Text("Back") }
            Button(onClick = onSyncNowClick, enabled = !state.isSyncing) {
                Text(if (state.isSyncing) "Syncing…" else "Sync now")
            }
        }

        state.syncErrorMessage?.let { message ->
            Text(text = message, color = MaterialTheme.colorScheme.error)
        }

        LazyColumn {
            items(state.items, key = { it.id }) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(text = item.valueText, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = item.factText ?: if (item.isSynced) "" else "Fact pending sync",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    IconButton(
                        onClick = { onToggleFavorite(item.id) },
                        modifier = Modifier.semantics {
                            contentDescription = if (item.isFavorite) "Remove from favorites" else "Add to favorites"
                        },
                    ) {
                        Text(text = if (item.isFavorite) "★" else "☆")
                    }
                }
            }
        }
    }
}
