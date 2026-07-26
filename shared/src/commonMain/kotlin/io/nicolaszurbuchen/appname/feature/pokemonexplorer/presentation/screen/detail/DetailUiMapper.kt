package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.detail

fun DetailState.toUiModel(): DetailUiModel =
    DetailUiModel(
        isLoading = isLoading,
        numberText = pokemon?.let { "#" + it.id.toString().padStart(3, '0') },
        name = pokemon?.name?.replaceFirstChar { char -> char.uppercase() },
        spriteUrl = pokemon?.spriteUrl,
        heightText = pokemon?.let { "${it.height / 10.0} m" },
        weightText = pokemon?.let { "${it.weight / 10.0} kg" },
    )
