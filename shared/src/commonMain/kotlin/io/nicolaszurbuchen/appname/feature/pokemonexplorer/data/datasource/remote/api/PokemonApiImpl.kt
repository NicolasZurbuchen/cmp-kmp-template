package io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.dto.PokemonDto

class PokemonApiImpl(
    private val client: HttpClient,
) : PokemonApi {
    override suspend fun getPokemon(id: Int): PokemonDto = client.get("https://pokeapi.co/api/v2/pokemon/$id").body()
}
