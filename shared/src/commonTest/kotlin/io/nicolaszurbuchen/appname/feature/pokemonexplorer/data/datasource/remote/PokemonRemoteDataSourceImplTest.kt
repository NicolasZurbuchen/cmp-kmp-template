package io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote

import io.nicolaszurbuchen.appname.common.error.AppError
import io.nicolaszurbuchen.appname.common.error.AppException
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.api.PokemonApi
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.dto.PokemonDto
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.dto.PokemonSpritesDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PokemonRemoteDataSourceImplTest {

    @Test
    fun fetchPokemon_apiSucceeds_returnsDtoUnchanged() = runTest {
        val dto = sampleDto()
        val api = FakePokemonApi(result = { dto })
        val dataSource = PokemonRemoteDataSourceImpl(api)

        val result = dataSource.fetchPokemon(25)

        assertEquals(dto, result)
    }

    @Test
    fun fetchPokemon_passesIdThroughToApi() = runTest {
        var capturedId: Int? = null
        val api = FakePokemonApi(result = { id -> capturedId = id; sampleDto() })
        val dataSource = PokemonRemoteDataSourceImpl(api)

        dataSource.fetchPokemon(42)

        assertEquals(42, capturedId)
    }

    @Test
    fun fetchPokemon_apiThrows_wrapsInPokemonExplorerFetchFailed() = runTest {
        val api = FakePokemonApi(result = { throw RuntimeException("boom") })
        val dataSource = PokemonRemoteDataSourceImpl(api)

        val exception = assertFailsWith<AppException> { dataSource.fetchPokemon(25) }

        assertEquals(AppError.PokemonExplorer.FetchFailed, exception.error)
    }

    @Test
    fun fetchPokemon_apiThrowsCancellation_propagatesUnwrapped() = runTest {
        val api = FakePokemonApi(result = { throw CancellationException("cancelled") })
        val dataSource = PokemonRemoteDataSourceImpl(api)

        assertFailsWith<CancellationException> { dataSource.fetchPokemon(25) }
    }

    private fun sampleDto(id: Int = 25) = PokemonDto(
        id = id,
        name = "pikachu",
        height = 4,
        weight = 60,
        sprites = PokemonSpritesDto(frontDefault = "sprite-url"),
    )

    private class FakePokemonApi(
        private val result: suspend (Int) -> PokemonDto,
    ) : PokemonApi {
        override suspend fun getPokemon(id: Int): PokemonDto = result(id)
    }
}
