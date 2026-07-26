package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history

import com.arkivanov.mvikotlin.core.store.Reducer

internal object HistoryReducer : Reducer<HistoryState, HistoryMessage> {
    override fun HistoryState.reduce(msg: HistoryMessage): HistoryState =
        when (msg) {
            is HistoryMessage.HistoryUpdated -> copy(items = msg.items)
            HistoryMessage.SyncStarted -> copy(isSyncing = true, syncError = null)
            is HistoryMessage.SyncFinished -> copy(isSyncing = false, syncError = msg.error)
        }
}
