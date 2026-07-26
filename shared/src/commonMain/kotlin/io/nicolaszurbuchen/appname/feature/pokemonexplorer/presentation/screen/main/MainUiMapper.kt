package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.main

import io.nicolaszurbuchen.appname.common.error.toUiModel

fun MainState.toUiModel(): MainUiModel {
    val items =
        history.map {
            PokemonItemUiModel(
                id = it.id,
                numberText = "#" + it.id.toString().padStart(3, '0'),
                name = it.name.replaceFirstChar { char -> char.uppercase() },
                spriteUrl = it.spriteUrl,
            )
        }

    return MainUiModel(
        isLoading = isLoading,
        hero = items.firstOrNull(),
        history = items.drop(1),
        error = error?.toUiModel(),
    )
}
