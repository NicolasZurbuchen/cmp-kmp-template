package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate

import io.nicolaszurbuchen.appname.common.error.AppError
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.model.GeneratedNumber

sealed interface GenerateIntent {
    data object GenerateClicked : GenerateIntent
}

sealed interface GenerateLabel

sealed interface GenerateAction

sealed interface GenerateMessage {
    data object GenerationStarted : GenerateMessage

    data class GenerationSucceeded(
        val number: GeneratedNumber,
    ) : GenerateMessage

    data class GenerationPartiallyFailed(
        val number: GeneratedNumber,
    ) : GenerateMessage

    data class GenerationFailed(
        val error: AppError,
    ) : GenerateMessage
}

data class GenerateState(
    val isLoading: Boolean = false,
    val lastGenerated: GeneratedNumber? = null,
    val error: AppError? = null,
)
