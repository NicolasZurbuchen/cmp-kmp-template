package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.main

import com.arkivanov.mvikotlin.core.store.Reducer

internal object MainReducer : Reducer<MainState, MainMessage> {
    override fun MainState.reduce(msg: MainMessage): MainState =
        when (msg) {
            MainMessage.GenerationStarted -> copy(isLoading = true, error = null)
            is MainMessage.HistoryUpdated -> copy(isLoading = false, history = msg.items)
            is MainMessage.GenerationFailed -> copy(isLoading = false, error = msg.error)
            MainMessage.ErrorDismissed -> copy(error = null)
        }
}
