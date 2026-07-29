package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.navigation

interface PokemonExplorerNavigator {
    fun navigateToDetail(historyId: Long)

    fun navigateBack()
}
