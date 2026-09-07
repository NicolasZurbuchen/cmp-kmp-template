package io.nicolaszurbuchen.appname.app.navigation

import androidx.navigation3.runtime.NavKey
import io.nicolaszurbuchen.appname.app.navigation.impl.PokemonExplorerNavigatorImpl
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.navigation.MainDestination
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.navigation.PokemonExplorerNavKeyHandler
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.navigation.PokemonExplorerNavigator
import io.nicolaszurbuchen.appname.infra.navigation.NavKeyHandler
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val appNavigationModule =
    module {
        single<NavKey>(named("initialRoute")) { MainDestination }

        singleOf(::PokemonExplorerNavigatorImpl) bind PokemonExplorerNavigator::class

        singleOf(::PokemonExplorerNavKeyHandler) { named("pokemonExplorer") } bind NavKeyHandler::class
    }
