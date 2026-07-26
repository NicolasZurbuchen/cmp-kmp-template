package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.detail

data class DetailUiModel(
    val isLoading: Boolean,
    val numberText: String?,
    val name: String?,
    val spriteUrl: String?,
    val heightText: String?,
    val weightText: String?,
)
