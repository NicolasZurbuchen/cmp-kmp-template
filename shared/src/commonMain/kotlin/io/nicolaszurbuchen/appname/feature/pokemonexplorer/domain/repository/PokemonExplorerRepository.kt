package io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.repository

import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.model.Pokemon
import kotlinx.coroutines.flow.Flow

interface PokemonExplorerRepository {
    suspend fun fetchRandomPokemon(): Pokemon

    fun observeHistory(): Flow<List<Pokemon>>

    suspend fun clearHistory()

    suspend fun getById(id: Int): Pokemon?
}
