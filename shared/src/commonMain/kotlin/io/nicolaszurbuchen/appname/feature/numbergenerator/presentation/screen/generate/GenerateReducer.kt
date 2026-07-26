package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate

import com.arkivanov.mvikotlin.core.store.Reducer
import io.nicolaszurbuchen.appname.common.error.AppError

internal object GenerateReducer : Reducer<GenerateState, GenerateMessage> {
    override fun GenerateState.reduce(msg: GenerateMessage): GenerateState =
        when (msg) {
            GenerateMessage.GenerationStarted -> copy(isLoading = true, error = null)

            is GenerateMessage.GenerationSucceeded -> copy(isLoading = false, lastGenerated = msg.number, error = null)

            is GenerateMessage.GenerationPartiallyFailed ->
                copy(
                    isLoading = false,
                    lastGenerated = msg.number,
                    error = AppError.NumberGenerator.FactFetchFailed,
                )

            is GenerateMessage.GenerationFailed -> copy(isLoading = false, error = msg.error)
        }
}
