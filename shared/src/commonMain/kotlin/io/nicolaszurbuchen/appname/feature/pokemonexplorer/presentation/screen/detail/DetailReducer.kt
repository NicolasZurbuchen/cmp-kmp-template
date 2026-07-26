package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.detail

import com.arkivanov.mvikotlin.core.store.Reducer

internal object DetailReducer : Reducer<DetailState, DetailMessage> {
    override fun DetailState.reduce(msg: DetailMessage): DetailState =
        when (msg) {
            is DetailMessage.PokemonLoaded -> copy(isLoading = false, pokemon = msg.pokemon)
        }
}
