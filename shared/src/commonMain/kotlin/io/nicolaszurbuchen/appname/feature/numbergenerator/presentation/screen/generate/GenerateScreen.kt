package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GenerateScreen(
    state: GenerateUiModel,
    onGenerateClick: () -> Unit,
    onHistoryClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.isLoading) {
            CircularProgressIndicator()
        }

        state.resultValue?.let { value ->
            Text(text = value, style = MaterialTheme.typography.displayMedium)
            state.resultFact?.let { fact -> Text(text = fact, style = MaterialTheme.typography.bodyLarge) }
        }

        state.errorMessage?.let { message ->
            Text(
                text = message,
                color =
                    if (state.isPartialFailureNotice) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
            )
        }

        Button(onClick = onGenerateClick, enabled = !state.isLoading) {
            Text("Generate a number")
        }

        TextButton(onClick = onHistoryClick) {
            Text("View history")
        }
    }
}
