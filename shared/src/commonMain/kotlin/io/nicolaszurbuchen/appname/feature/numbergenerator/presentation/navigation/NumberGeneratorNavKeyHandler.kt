package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate.GenerateRoute
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history.HistoryRoute
import io.nicolaszurbuchen.appname.infra.navigation.NavKeyHandler

class NumberGeneratorNavKeyHandler(
    private val navigator: NumberGeneratorNavigator,
) : NavKeyHandler {
    override fun EntryProviderScope<NavKey>.registerEntries() {
        entry<GenerateDestination> {
            GenerateRoute(onNavigateToHistory = { navigator.navigateToHistory() })
        }

        entry<HistoryDestination> {
            HistoryRoute(onNavigateBack = { navigator.navigateBack() })
        }
    }
}
