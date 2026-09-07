package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.nicolaszurbuchen.appname.design.preview.AppNamePreview
import io.nicolaszurbuchen.appname.infra.preview.PreviewThemes
import io.nicolaszurbuchen.appname.infra.text.UiText

/**
 * The skeleton, a loaded record, and the one nobody looks at until it happens: a row that resolved
 * to nothing, which this screen answers with a sentence rather than an empty page.
 */
private class DetailScreenStateProvider : PreviewParameterProvider<DetailUiModel> {
    override val values =
        sequenceOf(
            DetailUiModel(
                isLoading = true,
                numberText = null,
                name = null,
                spriteUrl = null,
                heightText = null,
                weightText = null,
            ),
            DetailUiModel(
                isLoading = false,
                numberText = "#001",
                name = "Bulbasaur",
                spriteUrl = "",
                heightText = UiText.Raw("0.7 m"),
                weightText = UiText.Raw("6.9 kg"),
            ),
            DetailUiModel(
                isLoading = false,
                numberText = null,
                name = null,
                spriteUrl = null,
                heightText = null,
                weightText = null,
            ),
        )
}

@PreviewThemes
@Composable
private fun DetailScreenPreview(
    @PreviewParameter(DetailScreenStateProvider::class) state: DetailUiModel,
) {
    AppNamePreview {
        DetailScreen(
            state = state,
            onBackClick = {},
        )
    }
}
