package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.nicolaszurbuchen.appname.core.error.AppErrorUiModel
import io.nicolaszurbuchen.appname.design.preview.AppNamePreview
import io.nicolaszurbuchen.appname.infra.preview.PreviewThemes
import io.nicolaszurbuchen.appname.infra.text.UiText

/**
 * Every state this screen can reach, in the order it reaches them. Scrolling the preview pane walks
 * a cold start: nothing, then a first result, then a history, then a failed refresh over the
 * history it kept.
 */
private class MainScreenStateProvider : PreviewParameterProvider<MainUiModel> {
    override val values =
        sequenceOf(
            MainUiModel(isLoading = true, hero = null, history = emptyList(), error = null),
            MainUiModel(isLoading = false, hero = null, history = emptyList(), error = null),
            MainUiModel(isLoading = false, hero = bulbasaur, history = emptyList(), error = null),
            MainUiModel(isLoading = false, hero = bulbasaur, history = history, error = null),
            // The failure that keeps its content: a refresh fell over and the list it already had is
            // still worth reading. The banner is the only thing that changes.
            MainUiModel(isLoading = false, hero = bulbasaur, history = history, error = offline),
        )

    private companion object {
        val bulbasaur =
            PokemonItemUiModel(
                historyId = 1,
                numberText = "#001",
                name = "Bulbasaur",
                spriteUrl = "",
            )

        val history =
            listOf(
                PokemonItemUiModel(historyId = 2, numberText = "#004", name = "Charmander", spriteUrl = ""),
                PokemonItemUiModel(historyId = 3, numberText = "#007", name = "Squirtle", spriteUrl = ""),
                PokemonItemUiModel(historyId = 4, numberText = "#025", name = "Pikachu", spriteUrl = ""),
            )

        val offline =
            AppErrorUiModel(
                title = UiText.Raw("No connection"),
                subtitle = UiText.Raw("Showing what was already downloaded."),
                icon = Icons.Default.CloudOff,
            )
    }
}

@PreviewThemes
@Composable
private fun MainScreenPreview(
    @PreviewParameter(MainScreenStateProvider::class) state: MainUiModel,
) {
    AppNamePreview {
        MainScreen(
            state = state,
            onGenerateClick = {},
            onItemClick = {},
            onClearClick = {},
            onRetryClick = {},
            onDismissErrorClick = {},
        )
    }
}
