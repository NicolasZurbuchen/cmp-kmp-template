package io.nicolaszurbuchen.appname.infra.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

/**
 * The host every destination is drawn into.
 *
 * [config] is passed in rather than read from a package, and that is what keeps this file in
 * `infra/`: the serializers module has to name every destination class, so it knows every feature,
 * and a file that knows every feature belongs in the composition root. Importing it from here made
 * the graph run `infra -> app`, which is the one direction `infra/` is defined by not taking.
 */
@Composable
fun NavGraph(
    config: SavedStateConfiguration,
    modifier: Modifier = Modifier,
) {
    val navigator = koinInject<AppNavigator>()
    val initialRoute = koinInject<NavKey>(named("initialRoute"))
    val handlers = getKoin().getAll<NavKeyHandler>()

    val backStack =
        rememberNavBackStack(
            config,
            initialRoute,
        )

    LaunchedEffect(backStack) {
        navigator.attach(backStack)
    }

    NavDisplay(
        backStack = backStack,
        modifier =
            modifier
                .background(color = MaterialTheme.colorScheme.background)
                .systemBarsPadding(),
        onBack = { backStack.removeLastOrNull() },
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        entryProvider =
            entryProvider {
                handlers.forEach { handler ->
                    with(handler) { registerEntries() }
                }
            },
    )
}
