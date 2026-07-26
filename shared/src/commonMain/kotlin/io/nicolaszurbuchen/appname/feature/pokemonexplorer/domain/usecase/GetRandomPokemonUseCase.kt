package io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.usecase

import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.model.Pokemon
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.repository.PokemonExplorerRepository

class GetRandomPokemonUseCase(
    private val repository: PokemonExplorerRepository,
) {
    suspend operator fun invoke(): Pokemon = repository.fetchRandomPokemon()
}
