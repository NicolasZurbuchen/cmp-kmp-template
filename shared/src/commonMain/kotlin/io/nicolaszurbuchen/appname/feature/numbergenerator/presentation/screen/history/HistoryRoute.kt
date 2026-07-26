package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryRoute(
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HistoryScreen(
        state = state,
        onToggleFavorite = { viewModel.onIntent(HistoryIntent.ToggleFavorite(it)) },
        onSyncNowClick = { viewModel.onIntent(HistoryIntent.SyncNowClicked) },
        onBackClick = onNavigateBack,
    )
}
