package io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote

import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.dto.PokemonDto

interface PokemonRemoteDataSource {
    suspend fun fetchPokemon(id: Int): PokemonDto
}
