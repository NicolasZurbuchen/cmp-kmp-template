package io.nicolaszurbuchen.appname

import androidx.compose.ui.window.ComposeUIViewController
import io.nicolaszurbuchen.appname.app.App
import io.nicolaszurbuchen.appname.app.di.initKoin
import org.koin.mp.KoinPlatform
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    if (KoinPlatform.getKoinOrNull() == null) {
        initKoin()
    }
    return ComposeUIViewController { App() }
}
