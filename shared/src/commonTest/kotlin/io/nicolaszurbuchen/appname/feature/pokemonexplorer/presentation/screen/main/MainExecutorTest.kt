package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.main

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.nicolaszurbuchen.appname.common.error.AppError
import io.nicolaszurbuchen.appname.common.error.AppException
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.fake.FakePokemonExplorerRepository
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.model.Pokemon
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.usecase.ClearHistoryUseCase
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.usecase.GetRandomPokemonUseCase
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.usecase.ObserveHistoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class MainExecutorTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        // MVIKotlin's CoroutineExecutor/Bootstrapper default to Dispatchers.Main;
        // this makes the executor's launched coroutines controllable via the test
        // dispatcher instead of real wall-clock scheduling.
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createStore(repository: FakePokemonExplorerRepository): MainStore =
        MainStoreFactory(
            storeFactory = DefaultStoreFactory(),
            getRandomPokemon = GetRandomPokemonUseCase(repository),
            observeHistory = ObserveHistoryUseCase(repository),
            clearHistory = ClearHistoryUseCase(repository),
        ).create()

    // region bootstrap / ObserveHistory

    @Test
    fun onCreate_immediatelyObservesEmptyHistoryFromRepository() = runTest {
        val repository = FakePokemonExplorerRepository()
        val store = createStore(repository)
        testDispatcher.scheduler.runCurrent()

        assertEquals(false, store.state.isLoading)
        assertEquals(emptyList(), store.state.history)
    }

    @Test
    fun onCreate_repositoryEmitsHistoryLater_updatesStateReactively() = runTest {
        val repository = FakePokemonExplorerRepository()
        val store = createStore(repository)
        testDispatcher.scheduler.runCurrent()
        val items = listOf(samplePokemon())

        repository.emitHistory(items)
        testDispatcher.scheduler.runCurrent()

        assertEquals(items, store.state.history)
    }

    // endregion

    // region GenerateClicked / RetryClicked

    @Test
    fun generateClicked_success_addsPokemonToHistoryAndClearsLoading() = runTest {
        val repository = FakePokemonExplorerRepository()
        val store = createStore(repository)
        testDispatcher.scheduler.runCurrent()

        store.accept(MainIntent.GenerateClicked)
        testDispatcher.scheduler.runCurrent()

        assertEquals(false, store.state.isLoading)
        assertEquals(null, store.state.error)
        assertEquals(listOf(repository.fetchRandomPokemonResult), store.state.history)
    }

    @Test
    fun generateClicked_repositoryThrowsAppException_setsMappedError() = runTest {
        val repository = FakePokemonExplorerRepository().apply {
            fetchRandomPokemonError = AppException(AppError.PokemonExplorer.FetchFailed)
        }
        val store = createStore(repository)
        testDispatcher.scheduler.runCurrent()

        store.accept(MainIntent.GenerateClicked)
        testDispatcher.scheduler.runCurrent()

        assertEquals(AppError.PokemonExplorer.FetchFailed, store.state.error)
    }

    @Test
    fun generateClicked_repositoryThrowsGenericException_wrapsInUnexpected() = runTest {
        val repository = FakePokemonExplorerRepository().apply {
            fetchRandomPokemonError = RuntimeException("boom")
        }
        val store = createStore(repository)
        testDispatcher.scheduler.runCurrent()

        store.accept(MainIntent.GenerateClicked)
        testDispatcher.scheduler.runCurrent()

        assertIs<AppError.Unexpected>(store.state.error)
    }

    @Test
    fun retryClicked_fetchesANewPokemonJustLikeGenerateClicked() = runTest {
        val repository = FakePokemonExplorerRepository()
        val store = createStore(repository)
        testDispatcher.scheduler.runCurrent()

        store.accept(MainIntent.RetryClicked)
        testDispatcher.scheduler.runCurrent()

        assertEquals(1, repository.fetchRandomPokemonCallCount)
    }

    // endregion

    // region ItemClicked

    @Test
    fun itemClicked_publishesNavigateToDetailLabelWithHistoryId() = runTest {
        val repository = FakePokemonExplorerRepository()
        val store = createStore(repository)
        testDispatcher.scheduler.runCurrent()

        store.labels.test {
            store.accept(MainIntent.ItemClicked(historyId = 42L))
            assertEquals(MainLabel.NavigateToDetail(42L), awaitItem())
        }
    }

    // endregion

    // region ClearClicked

    @Test
    fun clearClicked_delegatesToClearHistoryUseCase() = runTest {
        val repository = FakePokemonExplorerRepository()
        val store = createStore(repository)
        testDispatcher.scheduler.runCurrent()

        store.accept(MainIntent.ClearClicked)
        testDispatcher.scheduler.runCurrent()

        assertEquals(1, repository.clearHistoryCallCount)
    }

    // endregion

    // region DismissErrorClicked

    @Test
    fun dismissErrorClicked_clearsError() = runTest {
        val repository = FakePokemonExplorerRepository().apply {
            fetchRandomPokemonError = RuntimeException("boom")
        }
        val store = createStore(repository)
        testDispatcher.scheduler.runCurrent()
        store.accept(MainIntent.GenerateClicked)
        testDispatcher.scheduler.runCurrent()

        store.accept(MainIntent.DismissErrorClicked)
        testDispatcher.scheduler.runCurrent()

        assertEquals(null, store.state.error)
    }

    // endregion

    private fun samplePokemon() = Pokemon(
        historyId = 1L,
        speciesId = 25,
        name = "pikachu",
        spriteUrl = "https://example.com/pikachu.png",
        height = 4,
        weight = 60,
        fetchedAt = 1_000L,
    )
}
