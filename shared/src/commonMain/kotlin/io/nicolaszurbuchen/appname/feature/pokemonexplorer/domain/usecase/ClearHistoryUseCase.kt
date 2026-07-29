package io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.usecase

import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.repository.PokemonExplorerRepository

class ClearHistoryUseCase(
    private val repository: PokemonExplorerRepository,
) {
    suspend operator fun invoke() = repository.clearHistory()
}
