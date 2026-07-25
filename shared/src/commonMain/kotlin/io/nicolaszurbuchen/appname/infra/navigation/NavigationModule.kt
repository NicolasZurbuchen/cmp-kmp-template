package io.nicolaszurbuchen.appname.infra.navigation

import org.koin.dsl.module

val infraNavigationModule = module {
    single { AppNavigator() }
}
