package io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.repository

import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.local.PokemonLocalDataSource
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.local.mapper.toDomain
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.PokemonRemoteDataSource
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.mapper.toDomain
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.model.Pokemon
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.repository.PokemonExplorerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val MIN_ID = 1
private const val MAX_ID = 1025

class PokemonExplorerRepositoryImpl(
    private val remoteDataSource: PokemonRemoteDataSource,
    private val localDataSource: PokemonLocalDataSource,
) : PokemonExplorerRepository {
    @OptIn(ExperimentalTime::class)
    override suspend fun fetchRandomPokemon(): Pokemon {
        val id = Random.nextInt(MIN_ID, MAX_ID + 1)
        val fetchedAt = Clock.System.now().toEpochMilliseconds()
        val pokemon = remoteDataSource.fetchPokemon(id).toDomain(fetchedAt)

        localDataSource.insert(
            id = pokemon.id,
            name = pokemon.name,
            spriteUrl = pokemon.spriteUrl,
            height = pokemon.height,
            weight = pokemon.weight,
            fetchedAt = pokemon.fetchedAt,
        )

        return pokemon
    }

    override fun observeHistory(): Flow<List<Pokemon>> = localDataSource.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun clearHistory() {
        localDataSource.deleteAll()
    }

    override suspend fun getById(id: Int): Pokemon? = localDataSource.getById(id)?.toDomain()
}
