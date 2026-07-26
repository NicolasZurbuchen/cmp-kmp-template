package io.nicolaszurbuchen.appname.app.navigation

import androidx.navigation3.runtime.NavKey
import io.nicolaszurbuchen.appname.app.navigation.impl.NumberGeneratorNavigatorImpl
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.navigation.GenerateDestination
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.navigation.NumberGeneratorNavKeyHandler
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.navigation.NumberGeneratorNavigator
import io.nicolaszurbuchen.appname.infra.navigation.NavKeyHandler
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val appNavigationModule = module {
    single<NavKey>(named("initialRoute")) { GenerateDestination }

    singleOf(::NumberGeneratorNavigatorImpl) bind NumberGeneratorNavigator::class

    singleOf(::NumberGeneratorNavKeyHandler) { named("numberGenerator") } bind NavKeyHandler::class
}
