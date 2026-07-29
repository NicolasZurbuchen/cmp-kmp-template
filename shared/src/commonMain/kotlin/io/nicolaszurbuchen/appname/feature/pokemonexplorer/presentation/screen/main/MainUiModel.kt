package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.main

import io.nicolaszurbuchen.appname.common.error.AppErrorUiModel

data class MainUiModel(
    val isLoading: Boolean,
    val hero: PokemonItemUiModel?,
    val history: List<PokemonItemUiModel>,
    val error: AppErrorUiModel?,
)

data class PokemonItemUiModel(
    val historyId: Long,
    val numberText: String,
    val name: String,
    val spriteUrl: String,
)
