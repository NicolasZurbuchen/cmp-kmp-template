package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface NumberGeneratorDestination : NavKey

@Serializable
data object GenerateDestination : NumberGeneratorDestination

@Serializable
data object HistoryDestination : NumberGeneratorDestination
