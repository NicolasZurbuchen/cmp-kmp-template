package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.detail

import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.model.Pokemon

sealed interface DetailIntent

sealed interface DetailLabel

sealed interface DetailAction {
    data object LoadPokemon : DetailAction
}

sealed interface DetailMessage {
    data class PokemonLoaded(
        val pokemon: Pokemon?,
    ) : DetailMessage
}

data class DetailState(
    val isLoading: Boolean = true,
    val pokemon: Pokemon? = null,
)
