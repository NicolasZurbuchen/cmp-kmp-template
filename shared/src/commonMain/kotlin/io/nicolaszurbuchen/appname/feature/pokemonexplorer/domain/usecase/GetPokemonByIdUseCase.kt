package io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.usecase

import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.model.Pokemon
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.repository.PokemonExplorerRepository

class GetPokemonByIdUseCase(
    private val repository: PokemonExplorerRepository,
) {
    suspend operator fun invoke(historyId: Long): Pokemon? = repository.getById(historyId)
}
