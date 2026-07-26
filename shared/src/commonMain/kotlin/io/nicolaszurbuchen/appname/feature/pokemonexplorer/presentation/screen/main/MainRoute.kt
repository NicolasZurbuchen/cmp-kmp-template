package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainRoute(
    onNavigateToDetail: (Int) -> Unit,
    viewModel: MainViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onNavigateToDetailUpdated by rememberUpdatedState(onNavigateToDetail)

    LaunchedEffect(Unit) {
        viewModel.labels.collect { label ->
            when (label) {
                is MainLabel.NavigateToDetail -> onNavigateToDetailUpdated(label.id)
            }
        }
    }

    MainScreen(
        state = state,
        onGenerateClick = { viewModel.onIntent(MainIntent.GenerateClicked) },
        onItemClick = { id -> viewModel.onIntent(MainIntent.ItemClicked(id)) },
        onClearClick = { viewModel.onIntent(MainIntent.ClearClicked) },
        onRetryClick = { viewModel.onIntent(MainIntent.RetryClicked) },
        onDismissErrorClick = { viewModel.onIntent(MainIntent.DismissErrorClicked) },
    )
}
