package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GenerateRoute(
    onNavigateToHistory: () -> Unit,
    viewModel: GenerateViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    GenerateScreen(
        state = state,
        onGenerateClick = { viewModel.onIntent(GenerateIntent.GenerateClicked) },
        onHistoryClick = onNavigateToHistory,
    )
}
