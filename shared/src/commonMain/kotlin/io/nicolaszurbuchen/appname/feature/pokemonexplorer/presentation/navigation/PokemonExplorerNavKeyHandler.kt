package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.detail.DetailRoute
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.detail.DetailViewModel
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.main.MainRoute
import io.nicolaszurbuchen.appname.infra.navigation.NavKeyHandler
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

class PokemonExplorerNavKeyHandler(
    private val navigator: PokemonExplorerNavigator,
) : NavKeyHandler {
    override fun EntryProviderScope<NavKey>.registerEntries() {
        entry<MainDestination> {
            MainRoute(onNavigateToDetail = { id -> navigator.navigateToDetail(id) })
        }

        entry<DetailDestination> { destination ->
            DetailRoute(
                onNavigateBack = { navigator.navigateBack() },
                viewModel = koinViewModel<DetailViewModel>(parameters = { parametersOf(destination.id) }),
            )
        }
    }
}
