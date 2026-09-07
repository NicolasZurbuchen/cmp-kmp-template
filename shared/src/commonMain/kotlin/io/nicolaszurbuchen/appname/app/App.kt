package io.nicolaszurbuchen.appname.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.ktor.client.HttpClient
import io.nicolaszurbuchen.appname.design.theme.AppNameTheme
import io.nicolaszurbuchen.appname.infra.navigation.NavGraph
import org.koin.compose.koinInject

@Composable
fun App() {
    val httpClient = koinInject<HttpClient>()
    remember(Unit) {
        SingletonImageLoader.setSafe { context ->
            ImageLoader.Builder(context)
                .components { add(KtorNetworkFetcherFactory(httpClient = { httpClient })) }
                .build()
        }
    }

    AppNameTheme {
        NavGraph()
    }
}
