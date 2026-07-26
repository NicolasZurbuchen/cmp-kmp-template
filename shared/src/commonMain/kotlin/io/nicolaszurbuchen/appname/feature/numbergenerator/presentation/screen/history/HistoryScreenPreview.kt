@file:Suppress("ktlint:standard:filename")

package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.nicolaszurbuchen.appname.app.design.theme.AppNameTheme

class HistoryUiModelProvider : PreviewParameterProvider<HistoryUiModel> {
    override val values =
        sequenceOf(
            HistoryUiModel(
                items =
                    listOf(
                        HistoryItemUiModel(1, "42", "42 is the number of laws of cricket.", true, true),
                        HistoryItemUiModel(2, "7", null, false, false),
                    ),
                isSyncing = false,
                syncErrorMessage = null,
            ),
            HistoryUiModel(items = emptyList(), isSyncing = true, syncErrorMessage = null),
            HistoryUiModel(items = emptyList(), isSyncing = false, syncErrorMessage = "Sync failed. Try again."),
        )
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
private fun HistoryScreenPreview(
    @PreviewParameter(HistoryUiModelProvider::class) state: HistoryUiModel,
) {
    AppNameTheme {
        HistoryScreen(state = state, onToggleFavorite = {}, onSyncNowClick = {}, onBackClick = {})
    }
}
