package io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.local

import kotlinx.coroutines.flow.Flow

interface PokemonLocalDataSource {
    suspend fun insert(
        pokemonId: Int,
        name: String,
        spriteUrl: String,
        height: Int,
        weight: Int,
        fetchedAt: Long,
    ): Long

    fun observeAll(): Flow<List<CachedPokemon>>

    suspend fun getById(id: Long): CachedPokemon?

    suspend fun deleteAll()
}
