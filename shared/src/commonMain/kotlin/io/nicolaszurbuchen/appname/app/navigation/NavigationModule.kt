package io.nicolaszurbuchen.appname.app.navigation

import androidx.navigation3.runtime.NavKey
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appNavigationModule = module {
    single<NavKey>(named("initialRoute")) { InitialDestination }
}
