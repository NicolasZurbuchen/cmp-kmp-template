package io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.model

data class Pokemon(
    val historyId: Long,
    val speciesId: Int,
    val name: String,
    val spriteUrl: String,
    val height: Int,
    val weight: Int,
    val fetchedAt: Long,
)
