package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.navigation

interface PokemonExplorerNavigator {
    fun navigateToDetail(id: Int)

    fun navigateBack()
}
