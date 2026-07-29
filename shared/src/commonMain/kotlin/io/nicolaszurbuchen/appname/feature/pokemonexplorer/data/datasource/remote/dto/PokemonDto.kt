package io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PokemonDto(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val sprites: PokemonSpritesDto,
)
