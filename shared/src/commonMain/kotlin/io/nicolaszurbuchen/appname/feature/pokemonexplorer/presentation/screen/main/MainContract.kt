package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.main

import io.nicolaszurbuchen.appname.common.error.AppError
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.model.Pokemon

sealed interface MainIntent {
    data object GenerateClicked : MainIntent

    data class ItemClicked(
        val id: Int,
    ) : MainIntent

    data object ClearClicked : MainIntent

    data object RetryClicked : MainIntent

    data object DismissErrorClicked : MainIntent
}

sealed interface MainLabel {
    data class NavigateToDetail(
        val id: Int,
    ) : MainLabel
}

sealed interface MainAction {
    data object ObserveHistory : MainAction
}

sealed interface MainMessage {
    data object GenerationStarted : MainMessage

    data class HistoryUpdated(
        val items: List<Pokemon>,
    ) : MainMessage

    data class GenerationFailed(
        val error: AppError,
    ) : MainMessage

    data object ErrorDismissed : MainMessage
}

data class MainState(
    val isLoading: Boolean = false,
    val history: List<Pokemon> = emptyList(),
    val error: AppError? = null,
)
