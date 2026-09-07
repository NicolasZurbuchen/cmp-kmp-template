package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import appname.shared.generated.resources.Res
import appname.shared.generated.resources.pokemon_main_clear_list
import appname.shared.generated.resources.pokemon_main_empty_state
import appname.shared.generated.resources.pokemon_main_fab_description
import appname.shared.generated.resources.pokemon_main_history_title
import io.nicolaszurbuchen.appname.design.component.AppErrorBanner
import io.nicolaszurbuchen.appname.design.theme.ShimmerPulse
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.main.component.PokemonListItem
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.main.component.PokemonListItemShimmer
import org.jetbrains.compose.resources.stringResource

@Composable
fun MainScreen(
    state: MainUiModel,
    onGenerateClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    onClearClick: () -> Unit,
    onRetryClick: () -> Unit,
    onDismissErrorClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onGenerateClick) {
                Icon(Icons.Default.Refresh, contentDescription = stringResource(Res.string.pokemon_main_fab_description))
            }
        },
        modifier = modifier,
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            state.error?.let { error ->
                AppErrorBanner(
                    text = error.subtitle,
                    icon = error.icon,
                    onRetry = onRetryClick,
                    onDismiss = onDismissErrorClick,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }

            if (state.isLoading && state.hero == null) {
                ShimmerPulse { PokemonListItemShimmer(isHero = true) }
            } else {
                state.hero?.let { hero ->
                    PokemonListItem(
                        item = hero,
                        isHero = true,
                        onClick = { onItemClick(hero.historyId) },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(Res.string.pokemon_main_history_title), style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onClearClick, enabled = state.history.isNotEmpty()) {
                    Text(stringResource(Res.string.pokemon_main_clear_list))
                }
            }

            when {
                state.isLoading && state.history.isEmpty() && state.hero == null -> {
                    ShimmerPulse {
                        LazyColumn {
                            items(SKELETON_ROWS) { PokemonListItemShimmer() }
                        }
                    }
                }

                state.history.isEmpty() && state.hero == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(Res.string.pokemon_main_empty_state),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                else -> {
                    LazyColumn {
                        items(state.history, key = { it.historyId }) { item ->
                            PokemonListItem(
                                item = item,
                                onClick = { onItemClick(item.historyId) },
                            )
                        }
                    }
                }
            }
        }
    }
}

// Enough to read as a list rather than as one stray row, and few enough that none of them is still
// on screen when the real ones land on a short phone.
private const val SKELETON_ROWS = 3
