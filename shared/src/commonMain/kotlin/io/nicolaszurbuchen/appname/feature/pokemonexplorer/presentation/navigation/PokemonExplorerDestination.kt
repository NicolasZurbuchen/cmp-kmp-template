package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface PokemonExplorerDestination : NavKey

@Serializable
data object MainDestination : PokemonExplorerDestination

@Serializable
data class DetailDestination(
    val historyId: Long,
) : PokemonExplorerDestination
