@file:Suppress("ktlint:standard:filename")

package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.nicolaszurbuchen.appname.app.design.theme.AppNameTheme

class GenerateUiModelProvider : PreviewParameterProvider<GenerateUiModel> {
    override val values =
        sequenceOf(
            GenerateUiModel(isLoading = true, resultValue = null, resultFact = null, errorMessage = null, isPartialFailureNotice = false),
            GenerateUiModel(
                isLoading = false,
                resultValue = "42",
                resultFact = "42 is the number of laws of cricket.",
                errorMessage = null,
                isPartialFailureNotice = false,
            ),
            GenerateUiModel(
                isLoading = false,
                resultValue = "7",
                resultFact = null,
                errorMessage = "Saved without a fun fact — will sync later.",
                isPartialFailureNotice = true,
            ),
            GenerateUiModel(
                isLoading = false,
                resultValue = null,
                resultFact = null,
                errorMessage = "Couldn't reach the number generator. Try again.",
                isPartialFailureNotice = false,
            ),
        )
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
private fun GenerateScreenPreview(
    @PreviewParameter(GenerateUiModelProvider::class) state: GenerateUiModel,
) {
    AppNameTheme {
        GenerateScreen(state = state, onGenerateClick = {}, onHistoryClick = {})
    }
}
