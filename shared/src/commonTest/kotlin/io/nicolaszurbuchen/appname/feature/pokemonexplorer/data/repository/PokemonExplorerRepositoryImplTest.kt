package io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.repository

import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.local.CachedPokemon
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.local.PokemonLocalDataSource
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.PokemonRemoteDataSource
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.dto.PokemonDto
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.dto.PokemonSpritesDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PokemonExplorerRepositoryImplTest {
    // region fetchRandomPokemon

    @Test
    fun fetchRandomPokemon_requestsSpeciesIdWithinValidPokedexRange() =
        runTest {
            val remote = FakePokemonRemoteDataSource(dto = sampleDto())
            val local = FakePokemonLocalDataSource(insertResult = 1L)
            val repository = PokemonExplorerRepositoryImpl(remote, local, currentTimeMillis = { 42L })

            repository.fetchRandomPokemon()

            assertTrue(remote.lastRequestedId!! in 1..1025)
        }

    @Test
    fun fetchRandomPokemon_insertsFetchedPokemonIntoLocalDataSource() =
        runTest {
            val dto = sampleDto(id = 25, name = "pikachu", height = 4, weight = 60, frontDefault = "sprite-url")
            val remote = FakePokemonRemoteDataSource(dto = dto)
            val local = FakePokemonLocalDataSource(insertResult = 7L)
            val repository = PokemonExplorerRepositoryImpl(remote, local, currentTimeMillis = { 555L })

            repository.fetchRandomPokemon()

            val inserted = local.inserted.single()
            assertEquals(25, inserted.pokemonId)
            assertEquals("pikachu", inserted.name)
            assertEquals("sprite-url", inserted.spriteUrl)
            assertEquals(4, inserted.height)
            assertEquals(60, inserted.weight)
            assertEquals(555L, inserted.fetchedAt)
        }

    @Test
    fun fetchRandomPokemon_missingSprite_insertsEmptySpriteUrl() =
        runTest {
            val dto = sampleDto(frontDefault = null)
            val remote = FakePokemonRemoteDataSource(dto = dto)
            val local = FakePokemonLocalDataSource(insertResult = 1L)
            val repository = PokemonExplorerRepositoryImpl(remote, local)

            repository.fetchRandomPokemon()

            assertEquals("", local.inserted.single().spriteUrl)
        }

    @Test
    fun fetchRandomPokemon_returnsDomainPokemonWithHistoryIdFromLocalInsert() =
        runTest {
            val dto = sampleDto(id = 25, name = "pikachu")
            val remote = FakePokemonRemoteDataSource(dto = dto)
            val local = FakePokemonLocalDataSource(insertResult = 99L)
            val repository = PokemonExplorerRepositoryImpl(remote, local, currentTimeMillis = { 555L })

            val result = repository.fetchRandomPokemon()

            assertEquals(99L, result.historyId)
            assertEquals(25, result.speciesId)
            assertEquals("pikachu", result.name)
            assertEquals(555L, result.fetchedAt)
        }

    // endregion

    // region observeHistory

    @Test
    fun observeHistory_mapsLocalRowsToDomainPokemonList() =
        runTest {
            val cached =
                CachedPokemon(id = 1L, pokemon_id = 25L, name = "pikachu", sprite_url = "url", height = 4L, weight = 60L, fetched_at = 100L)
            val local = FakePokemonLocalDataSource(observeAllResult = flowOf(listOf(cached)))
            val remote = FakePokemonRemoteDataSource(dto = sampleDto())
            val repository = PokemonExplorerRepositoryImpl(remote, local)

            val result = repository.observeHistory().first()

            assertEquals(1, result.size)
            assertEquals(1L, result.first().historyId)
            assertEquals(25, result.first().speciesId)
        }

    // endregion

    // region clearHistory

    @Test
    fun clearHistory_delegatesToLocalDataSource() =
        runTest {
            val local = FakePokemonLocalDataSource()
            val remote = FakePokemonRemoteDataSource(dto = sampleDto())
            val repository = PokemonExplorerRepositoryImpl(remote, local)

            repository.clearHistory()

            assertEquals(1, local.deleteAllCallCount)
        }

    // endregion

    // region getById

    @Test
    fun getById_existingId_returnsMappedDomainPokemon() =
        runTest {
            val cached =
                CachedPokemon(id = 3L, pokemon_id = 25L, name = "pikachu", sprite_url = "url", height = 4L, weight = 60L, fetched_at = 100L)
            val local = FakePokemonLocalDataSource(getByIdResults = mapOf(3L to cached))
            val remote = FakePokemonRemoteDataSource(dto = sampleDto())
            val repository = PokemonExplorerRepositoryImpl(remote, local)

            val result = repository.getById(3L)

            assertEquals(3L, result?.historyId)
            assertEquals(25, result?.speciesId)
        }

    @Test
    fun getById_missingId_returnsNull() =
        runTest {
            val local = FakePokemonLocalDataSource()
            val remote = FakePokemonRemoteDataSource(dto = sampleDto())
            val repository = PokemonExplorerRepositoryImpl(remote, local)

            assertNull(repository.getById(999L))
        }

    // endregion

    private fun sampleDto(
        id: Int = 25,
        name: String = "pikachu",
        height: Int = 4,
        weight: Int = 60,
        frontDefault: String? = "sprite-url",
    ) = PokemonDto(
        id = id,
        name = name,
        height = height,
        weight = weight,
        sprites = PokemonSpritesDto(frontDefault = frontDefault),
    )

    private class FakePokemonRemoteDataSource(
        private val dto: PokemonDto,
    ) : PokemonRemoteDataSource {
        var lastRequestedId: Int? = null
            private set

        override suspend fun fetchPokemon(id: Int): PokemonDto {
            lastRequestedId = id
            return dto
        }
    }

    private class FakePokemonLocalDataSource(
        private val insertResult: Long = 1L,
        private val observeAllResult: Flow<List<CachedPokemon>> = flowOf(emptyList()),
        private val getByIdResults: Map<Long, CachedPokemon?> = emptyMap(),
    ) : PokemonLocalDataSource {
        data class Inserted(
            val pokemonId: Int,
            val name: String,
            val spriteUrl: String,
            val height: Int,
            val weight: Int,
            val fetchedAt: Long,
        )

        val inserted = mutableListOf<Inserted>()
        var deleteAllCallCount = 0
            private set

        override suspend fun insert(
            pokemonId: Int,
            name: String,
            spriteUrl: String,
            height: Int,
            weight: Int,
            fetchedAt: Long,
        ): Long {
            inserted += Inserted(pokemonId, name, spriteUrl, height, weight, fetchedAt)
            return insertResult
        }

        override fun observeAll(): Flow<List<CachedPokemon>> = observeAllResult

        override suspend fun getById(id: Long): CachedPokemon? = getByIdResults[id]

        override suspend fun deleteAll() {
            deleteAllCallCount++
        }
    }
}
