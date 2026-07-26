package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate

import io.nicolaszurbuchen.appname.common.error.AppError

fun GenerateState.toUiModel(): GenerateUiModel =
    GenerateUiModel(
        isLoading = isLoading,
        resultValue = lastGenerated?.value?.toString(),
        resultFact = lastGenerated?.fact,
        errorMessage = error?.toMessage(lastGenerated != null),
        isPartialFailureNotice = error == AppError.NumberGenerator.FactFetchFailed,
    )

private fun AppError.toMessage(hasResult: Boolean): String? =
    when (this) {
        AppError.NumberGenerator.NumberFetchFailed -> "Couldn't reach the number generator. Try again."
        AppError.NumberGenerator.FactFetchFailed ->
            if (hasResult) "Saved without a fun fact — will sync later." else null
        else -> "Something went wrong. Try again."
    }
