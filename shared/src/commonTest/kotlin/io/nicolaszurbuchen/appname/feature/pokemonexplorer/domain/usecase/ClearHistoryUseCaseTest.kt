package io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.usecase

import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.fake.FakePokemonExplorerRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ClearHistoryUseCaseTest {
    @Test
    fun invoke_delegatesDirectlyToRepository() =
        runTest {
            val repository = FakePokemonExplorerRepository()
            val useCase = ClearHistoryUseCase(repository)

            useCase()

            assertEquals(1, repository.clearHistoryCallCount)
        }

    @Test
    fun invoke_repositoryThrows_propagatesException() =
        runTest {
            val repository =
                FakePokemonExplorerRepository().apply {
                    clearHistoryError = RuntimeException("disk full")
                }
            val useCase = ClearHistoryUseCase(repository)

            assertFailsWith<RuntimeException> { useCase() }
        }
}
