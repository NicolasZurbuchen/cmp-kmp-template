package io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.api

import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.dto.PokemonDto

interface PokemonApi {
    suspend fun getPokemon(id: Int): PokemonDto
}
