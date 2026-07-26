package io.nicolaszurbuchen.appname.app.navigation.impl

import io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.navigation.DetailDestination
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.navigation.PokemonExplorerNavigator
import io.nicolaszurbuchen.appname.infra.navigation.AppNavigator

class PokemonExplorerNavigatorImpl(
    private val appNavigator: AppNavigator,
) : PokemonExplorerNavigator {
    override fun navigateToDetail(id: Int) {
        appNavigator.navigateTo(DetailDestination(id))
    }

    override fun navigateBack() {
        appNavigator.navigateBack()
    }
}
