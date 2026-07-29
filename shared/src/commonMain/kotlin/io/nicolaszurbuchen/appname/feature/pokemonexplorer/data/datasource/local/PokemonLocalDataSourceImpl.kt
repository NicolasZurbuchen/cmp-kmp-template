package io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class PokemonLocalDataSourceImpl(
    private val queries: CachedPokemonQueries,
) : PokemonLocalDataSource {
    override suspend fun insert(
        pokemonId: Int,
        name: String,
        spriteUrl: String,
        height: Int,
        weight: Int,
        fetchedAt: Long,
    ): Long =
        queries.transactionWithResult {
            queries.insertPokemon(
                pokemon_id = pokemonId.toLong(),
                name = name,
                sprite_url = spriteUrl,
                height = height.toLong(),
                weight = weight.toLong(),
                fetched_at = fetchedAt,
            )
            queries.lastInsertRowId().executeAsOne()
        }

    override fun observeAll(): Flow<List<CachedPokemon>> = queries.selectAllOrderByFetchedAtDesc().asFlow().mapToList(Dispatchers.Default)

    override suspend fun getById(id: Long): CachedPokemon? = queries.selectById(id).executeAsOneOrNull()

    override suspend fun deleteAll() {
        queries.deleteAll()
    }
}
