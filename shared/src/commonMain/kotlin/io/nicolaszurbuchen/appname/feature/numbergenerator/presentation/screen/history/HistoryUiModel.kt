package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history

data class HistoryUiModel(
    val items: List<HistoryItemUiModel>,
    val isSyncing: Boolean,
    val syncErrorMessage: String?,
)

data class HistoryItemUiModel(
    val id: Long,
    val valueText: String,
    val factText: String?,
    val isFavorite: Boolean,
    val isSynced: Boolean,
)
