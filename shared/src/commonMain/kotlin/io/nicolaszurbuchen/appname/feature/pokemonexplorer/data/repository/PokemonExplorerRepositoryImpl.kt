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

class PokemonExplorerRepositoryImpl(
    private val remoteDataSource: PokemonRemoteDataSource,
    private val localDataSource: PokemonLocalDataSource,
    private val random: Random = Random.Default,
) : PokemonExplorerRepository {
    companion object {
        // PokéAPI's /pokemon/{id} endpoint currently resolves ids 1 through 1025
        // (the full national Pokédex range at the time this feature was built).
        private const val MIN_POKEMON_ID = 1
        private const val MAX_POKEMON_ID = 1025
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun fetchRandomPokemon(): Pokemon {
        val speciesId = random.nextInt(MIN_POKEMON_ID, MAX_POKEMON_ID + 1)
        val fetchedAt = Clock.System.now().toEpochMilliseconds()
        val dto = remoteDataSource.fetchPokemon(speciesId)

        val historyId =
            localDataSource.insert(
                pokemonId = dto.id,
                name = dto.name,
                spriteUrl = dto.sprites.frontDefault.orEmpty(),
                height = dto.height,
                weight = dto.weight,
                fetchedAt = fetchedAt,
            )

        return dto.toDomain(historyId = historyId, fetchedAt = fetchedAt)
    }

    override fun observeHistory(): Flow<List<Pokemon>> = localDataSource.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun clearHistory() {
        localDataSource.deleteAll()
    }

    override suspend fun getById(historyId: Long): Pokemon? = localDataSource.getById(historyId)?.toDomain()
}
