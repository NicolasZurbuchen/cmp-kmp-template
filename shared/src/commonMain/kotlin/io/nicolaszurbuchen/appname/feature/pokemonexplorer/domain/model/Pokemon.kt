package io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.model

data class Pokemon(
    val id: Int,
    val name: String,
    val spriteUrl: String,
    val height: Int,
    val weight: Int,
    val fetchedAt: Long,
)
