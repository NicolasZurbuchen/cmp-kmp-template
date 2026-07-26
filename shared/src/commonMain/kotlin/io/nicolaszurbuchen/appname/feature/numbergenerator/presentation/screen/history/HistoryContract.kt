package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history

import io.nicolaszurbuchen.appname.common.error.AppError
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.model.GeneratedNumber

sealed interface HistoryIntent {
    data class ToggleFavorite(
        val id: Long,
    ) : HistoryIntent

    data object SyncNowClicked : HistoryIntent
}

sealed interface HistoryLabel

sealed interface HistoryAction {
    data object ObserveHistory : HistoryAction
}

sealed interface HistoryMessage {
    data class HistoryUpdated(
        val items: List<GeneratedNumber>,
    ) : HistoryMessage

    data object SyncStarted : HistoryMessage

    data class SyncFinished(
        val error: AppError?,
    ) : HistoryMessage
}

data class HistoryState(
    val items: List<GeneratedNumber> = emptyList(),
    val isSyncing: Boolean = false,
    val syncError: AppError? = null,
)
