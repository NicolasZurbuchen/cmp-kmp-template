package io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.local.mapper

import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.local.CachedPokemon
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.model.Pokemon

fun CachedPokemon.toDomain(): Pokemon =
    Pokemon(
        id = id.toInt(),
        name = name,
        spriteUrl = sprite_url,
        height = height.toInt(),
        weight = weight.toInt(),
        fetchedAt = fetched_at,
    )
