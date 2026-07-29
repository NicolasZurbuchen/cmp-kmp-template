package io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.fake

import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.model.Pokemon
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.repository.PokemonExplorerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePokemonExplorerRepository : PokemonExplorerRepository {
    // fetchRandomPokemon
    var fetchRandomPokemonResult: Pokemon = defaultPokemon
    var fetchRandomPokemonError: Throwable? = null
    var fetchRandomPokemonCallCount = 0
        private set

    // observeHistory
    private val historyFlow = MutableStateFlow<List<Pokemon>>(emptyList())

    // clearHistory
    var clearHistoryError: Throwable? = null
    var clearHistoryCallCount = 0
        private set

    // getById
    var getByIdResults: Map<Long, Pokemon?> = emptyMap()
    var lastRequestedHistoryId: Long? = null
        private set

    override suspend fun fetchRandomPokemon(): Pokemon {
        fetchRandomPokemonCallCount++
        fetchRandomPokemonError?.let { throw it }
        historyFlow.value = listOf(fetchRandomPokemonResult) + historyFlow.value
        return fetchRandomPokemonResult
    }

    override fun observeHistory(): Flow<List<Pokemon>> = historyFlow

    override suspend fun clearHistory() {
        clearHistoryCallCount++
        clearHistoryError?.let { throw it }
        historyFlow.value = emptyList()
    }

    override suspend fun getById(historyId: Long): Pokemon? {
        lastRequestedHistoryId = historyId
        if (getByIdResults.containsKey(historyId)) return getByIdResults[historyId]
        return historyFlow.value.find { it.historyId == historyId }
    }

    fun emitHistory(items: List<Pokemon>) {
        historyFlow.value = items
    }

    companion object {
        private val defaultPokemon =
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
}
