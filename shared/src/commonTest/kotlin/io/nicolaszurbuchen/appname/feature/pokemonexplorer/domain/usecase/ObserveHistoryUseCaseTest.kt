package io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.usecase

import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.fake.FakePokemonExplorerRepository
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.model.Pokemon
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveHistoryUseCaseTest {
    @Test
    fun invoke_delegatesDirectlyToRepository() =
        runTest {
            val repository = FakePokemonExplorerRepository()
            val items = listOf(samplePokemon())
            repository.emitHistory(items)
            val useCase = ObserveHistoryUseCase(repository)

            assertEquals(items, useCase().first())
        }

    private fun samplePokemon() =
        Pokemon(
            historyId = 1L,
            speciesId = 25,
            name = "pikachu",
            spriteUrl = "https://example.com/pikachu.png",
            height = 4,
            weight = 60,
            fetchedAt = 1_000L,
        )
}
