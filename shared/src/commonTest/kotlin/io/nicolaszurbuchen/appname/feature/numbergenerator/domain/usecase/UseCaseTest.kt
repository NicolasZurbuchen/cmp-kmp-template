package io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase

import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.model.GeneratedNumber
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.repository.NumberGeneratorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeNumberGeneratorRepository : NumberGeneratorRepository {
    var generateAndSaveResult: GeneratedNumber = GeneratedNumber(1, 42, "a fact", 1_000L, false, true)
    var historyFlow: Flow<List<GeneratedNumber>> = flowOf(emptyList())
    var toggleFavoriteCalledWith: Long? = null
    var syncPendingCalled = false

    override suspend fun generateAndSave(): GeneratedNumber = generateAndSaveResult

    override fun observeHistory(): Flow<List<GeneratedNumber>> = historyFlow

    override suspend fun toggleFavorite(id: Long) {
        toggleFavoriteCalledWith = id
    }

    override suspend fun syncPending() {
        syncPendingCalled = true
    }
}

class GenerateNumberUseCaseTest {
    @Test
    fun `invoke returns the repository result`() =
        runTest {
            val repository = FakeNumberGeneratorRepository()
            val useCase = GenerateNumberUseCase(repository)

            val result = useCase()

            assertEquals(repository.generateAndSaveResult, result)
        }
}

class ObserveHistoryUseCaseTest {
    @Test
    fun `invoke returns the repository history flow`() =
        runTest {
            val expected = listOf(GeneratedNumber(1, 7, null, 500L, true, false))
            val repository = FakeNumberGeneratorRepository().apply { historyFlow = flowOf(expected) }
            val useCase = ObserveHistoryUseCase(repository)

            val result = useCase().let { flow -> mutableListOf<List<GeneratedNumber>>().also { list -> flow.collect { list.add(it) } } }

            assertEquals(listOf(expected), result)
        }
}

class ToggleFavoriteUseCaseTest {
    @Test
    fun `invoke forwards the id to the repository`() =
        runTest {
            val repository = FakeNumberGeneratorRepository()
            val useCase = ToggleFavoriteUseCase(repository)

            useCase(42L)

            assertEquals(42L, repository.toggleFavoriteCalledWith)
        }
}

class SyncPendingUseCaseTest {
    @Test
    fun `invoke calls syncPending on the repository`() =
        runTest {
            val repository = FakeNumberGeneratorRepository()
            val useCase = SyncPendingUseCase(repository)

            useCase()

            assertTrue(repository.syncPendingCalled)
        }
}
