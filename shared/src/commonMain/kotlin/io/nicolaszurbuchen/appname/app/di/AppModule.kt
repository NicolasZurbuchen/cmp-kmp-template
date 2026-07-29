package io.nicolaszurbuchen.appname.app.di

import io.nicolaszurbuchen.appname.app.navigation.appNavigationModule
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.di.pokemonExplorerModule
import io.nicolaszurbuchen.appname.infra.database.databaseModule
import io.nicolaszurbuchen.appname.infra.mvi.storeModule
import io.nicolaszurbuchen.appname.infra.navigation.infraNavigationModule
import io.nicolaszurbuchen.appname.infra.network.networkModule

val appModule =
    listOf(
        appNavigationModule,
        databaseModule,
        infraNavigationModule,
        networkModule,
        storeModule,
        pokemonExplorerModule,
    )
