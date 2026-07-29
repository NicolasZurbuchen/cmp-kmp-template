package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.detail

import io.nicolaszurbuchen.appname.infra.ui.UiText

data class DetailUiModel(
    val isLoading: Boolean,
    val numberText: String?,
    val name: String?,
    val spriteUrl: String?,
    val heightText: UiText?,
    val weightText: UiText?,
)
