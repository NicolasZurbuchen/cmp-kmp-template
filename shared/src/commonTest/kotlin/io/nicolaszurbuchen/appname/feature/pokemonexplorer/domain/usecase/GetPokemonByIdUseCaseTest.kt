package io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.usecase

import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.fake.FakePokemonExplorerRepository
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.model.Pokemon
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetPokemonByIdUseCaseTest {
    @Test
    fun invoke_delegatesDirectlyToRepository() =
        runTest {
            val pokemon = samplePokemon(historyId = 5L)
            val repository = FakePokemonExplorerRepository().apply { getByIdResults = mapOf(5L to pokemon) }
            val useCase = GetPokemonByIdUseCase(repository)

            assertEquals(pokemon, useCase(5L))
            assertEquals(5L, repository.lastRequestedHistoryId)
        }

    @Test
    fun invoke_unknownHistoryId_returnsNull() =
        runTest {
            val repository = FakePokemonExplorerRepository()
            val useCase = GetPokemonByIdUseCase(repository)

            assertNull(useCase(999L))
        }

    private fun samplePokemon(historyId: Long) =
        Pokemon(
            historyId = historyId,
            speciesId = 25,
            name = "pikachu",
            spriteUrl = "https://example.com/pikachu.png",
            height = 4,
            weight = 60,
            fetchedAt = 1_000L,
        )
}
