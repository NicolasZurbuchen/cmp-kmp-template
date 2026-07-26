package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate

data class GenerateUiModel(
    val isLoading: Boolean,
    val resultValue: String?,
    val resultFact: String?,
    val errorMessage: String?,
    val isPartialFailureNotice: Boolean,
)
