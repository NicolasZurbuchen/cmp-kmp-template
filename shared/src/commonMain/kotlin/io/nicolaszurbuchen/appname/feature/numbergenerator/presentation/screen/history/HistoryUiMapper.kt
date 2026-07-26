package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history

fun HistoryState.toUiModel(): HistoryUiModel =
    HistoryUiModel(
        items =
            items.map { number ->
                HistoryItemUiModel(
                    id = number.id,
                    valueText = number.value.toString(),
                    factText = number.fact,
                    isFavorite = number.isFavorite,
                    isSynced = number.isSynced,
                )
            },
        isSyncing = isSyncing,
        syncErrorMessage = syncError?.let { "Sync failed. Try again." },
    )
