package io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote

import io.nicolaszurbuchen.appname.common.error.AppError
import io.nicolaszurbuchen.appname.common.error.AppException
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.api.PokemonApi
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.dto.PokemonDto
import kotlinx.coroutines.CancellationException

class PokemonRemoteDataSourceImpl(
    private val api: PokemonApi,
) : PokemonRemoteDataSource {
    override suspend fun fetchPokemon(id: Int): PokemonDto =
        try {
            api.getPokemon(id)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            throw AppException(AppError.PokemonExplorer.FetchFailed)
        }
}
