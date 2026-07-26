package io.nicolaszurbuchen.appname.app

import androidx.compose.runtime.Composable
import io.nicolaszurbuchen.appname.app.design.theme.AppNameTheme
import io.nicolaszurbuchen.appname.infra.navigation.NavGraph

@Composable
fun App() {
    AppNameTheme {
        NavGraph()
    }
}
