package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.detail

import appname.shared.generated.resources.Res
import appname.shared.generated.resources.pokemon_detail_height_label
import appname.shared.generated.resources.pokemon_detail_weight_label
import io.nicolaszurbuchen.appname.infra.text.UiText

fun DetailState.toUiModel(): DetailUiModel =
    DetailUiModel(
        isLoading = isLoading,
        numberText = pokemon?.let { "#" + it.speciesId.toString().padStart(3, '0') },
        name = pokemon?.name?.replaceFirstChar { char -> char.uppercase() },
        spriteUrl = pokemon?.spriteUrl,
        heightText =
            pokemon?.let {
                UiText.Resource(Res.string.pokemon_detail_height_label, listOf("${it.height / 10.0} m"))
            },
        weightText =
            pokemon?.let {
                UiText.Resource(Res.string.pokemon_detail_weight_label, listOf("${it.weight / 10.0} kg"))
            },
    )
