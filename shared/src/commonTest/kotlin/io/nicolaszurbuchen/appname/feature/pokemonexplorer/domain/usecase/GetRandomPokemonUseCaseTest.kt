package io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.usecase

import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.fake.FakePokemonExplorerRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetRandomPokemonUseCaseTest {

    @Test
    fun invoke_delegatesDirectlyToRepository() = runTest {
        val repository = FakePokemonExplorerRepository()
        val expected = repository.fetchRandomPokemonResult
        val useCase = GetRandomPokemonUseCase(repository)

        assertEquals(expected, useCase())
    }

    @Test
    fun invoke_repositoryThrows_propagatesException() = runTest {
        val repository = FakePokemonExplorerRepository().apply {
            fetchRandomPokemonError = RuntimeException("network down")
        }
        val useCase = GetRandomPokemonUseCase(repository)

        assertFailsWith<RuntimeException> { useCase() }
    }
}
