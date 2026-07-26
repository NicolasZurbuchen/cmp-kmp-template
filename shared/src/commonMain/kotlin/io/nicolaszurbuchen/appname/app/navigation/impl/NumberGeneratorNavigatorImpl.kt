package io.nicolaszurbuchen.appname.app.navigation.impl

import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.navigation.HistoryDestination
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.navigation.NumberGeneratorNavigator
import io.nicolaszurbuchen.appname.infra.navigation.AppNavigator

class NumberGeneratorNavigatorImpl(
    private val appNavigator: AppNavigator,
) : NumberGeneratorNavigator {
    override fun navigateToHistory() {
        appNavigator.navigateTo(HistoryDestination)
    }

    override fun navigateBack() {
        appNavigator.navigateBack()
    }
}
