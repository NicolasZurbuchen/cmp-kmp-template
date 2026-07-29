package io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.mapper

import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.dto.PokemonDto
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.model.Pokemon

fun PokemonDto.toDomain(
    historyId: Long,
    fetchedAt: Long,
): Pokemon =
    Pokemon(
        historyId = historyId,
        speciesId = id,
        name = name,
        spriteUrl = sprites.frontDefault.orEmpty(),
        height = height,
        weight = weight,
        fetchedAt = fetchedAt,
    )
