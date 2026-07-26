package io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class PokemonLocalDataSourceImpl(
    private val queries: CachedPokemonQueries,
) : PokemonLocalDataSource {
    override suspend fun insert(
        id: Int,
        name: String,
        spriteUrl: String,
        height: Int,
        weight: Int,
        fetchedAt: Long,
    ) {
        queries.insertPokemon(
            id = id.toLong(),
            name = name,
            sprite_url = spriteUrl,
            height = height.toLong(),
            weight = weight.toLong(),
            fetched_at = fetchedAt,
        )
    }

    override fun observeAll(): Flow<List<CachedPokemon>> = queries.selectAllOrderByFetchedAtDesc().asFlow().mapToList(Dispatchers.Default)

    override suspend fun getById(id: Int): CachedPokemon? = queries.selectById(id.toLong()).executeAsOneOrNull()

    override suspend fun deleteAll() {
        queries.deleteAll()
    }
}
