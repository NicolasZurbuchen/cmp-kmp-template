package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.detail

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.fake.FakePokemonExplorerRepository
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.model.Pokemon
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.usecase.GetPokemonByIdUseCase
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
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class DetailExecutorTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createStore(
        repository: FakePokemonExplorerRepository,
        historyId: Long,
    ): DetailStore =
        DetailStoreFactory(
            storeFactory = DefaultStoreFactory(),
            getPokemonById = GetPokemonByIdUseCase(repository),
            historyId = historyId,
        ).create()

    @Test
    fun onCreate_loadsPokemonMatchingHistoryId() = runTest {
        val pokemon = samplePokemon(historyId = 7L)
        val repository = FakePokemonExplorerRepository().apply { getByIdResults = mapOf(7L to pokemon) }
        val store = createStore(repository, historyId = 7L)

        testDispatcher.scheduler.runCurrent()

        assertEquals(false, store.state.isLoading)
        assertEquals(pokemon, store.state.pokemon)
    }

    @Test
    fun onCreate_unknownHistoryId_setsNullPokemon() = runTest {
        val repository = FakePokemonExplorerRepository()
        val store = createStore(repository, historyId = 999L)

        testDispatcher.scheduler.runCurrent()

        assertEquals(false, store.state.isLoading)
        assertNull(store.state.pokemon)
    }

    @Test
    fun onCreate_requestsPokemonForConstructorHistoryId() = runTest {
        val repository = FakePokemonExplorerRepository()
        createStore(repository, historyId = 42L)

        testDispatcher.scheduler.runCurrent()

        assertEquals(42L, repository.lastRequestedHistoryId)
    }

    private fun samplePokemon(historyId: Long) = Pokemon(
        historyId = historyId,
        speciesId = 25,
        name = "pikachu",
        spriteUrl = "https://example.com/pikachu.png",
        height = 4,
        weight = 60,
        fetchedAt = 1_000L,
    )
}
