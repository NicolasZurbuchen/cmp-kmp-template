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
import io.nicolaszurbuchen.appname.app.design.component.AppErrorBanner
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.component.PokemonListItem
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.component.PokemonListItemShimmer

@Composable
fun MainScreen(
    state: MainUiModel,
    onGenerateClick: () -> Unit,
    onItemClick: (Int) -> Unit,
    onClearClick: () -> Unit,
    onRetryClick: () -> Unit,
    onDismissErrorClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = onGenerateClick) {
                Icon(Icons.Default.Refresh, contentDescription = "Find a random Pokémon")
            }
        },
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
                PokemonListItemShimmer(isHero = true)
            } else {
                state.hero?.let { hero ->
                    PokemonListItem(
                        numberText = hero.numberText,
                        name = hero.name,
                        spriteUrl = hero.spriteUrl,
                        isHero = true,
                        onClick = { onItemClick(hero.id) },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "History", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onClearClick, enabled = state.history.isNotEmpty()) {
                    Text("Clear list")
                }
            }

            when {
                state.isLoading && state.history.isEmpty() && state.hero == null -> {
                    LazyColumn {
                        items(3) { PokemonListItemShimmer() }
                    }
                }

                state.history.isEmpty() && state.hero == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Tap the button below to discover your first Pokémon",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                else -> {
                    LazyColumn {
                        items(state.history, key = { it.id }) { item ->
                            PokemonListItem(
                                numberText = item.numberText,
                                name = item.name,
                                spriteUrl = item.spriteUrl,
                                onClick = { onItemClick(item.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}
