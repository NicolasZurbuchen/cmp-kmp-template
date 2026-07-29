package io.nicolaszurbuchen.appname

import android.app.Application
import io.nicolaszurbuchen.appname.app.di.initKoin
import io.nicolaszurbuchen.appname.infra.di.platformModule
import org.koin.android.ext.koin.androidContext

class AppNameApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(
            additionalModules = listOf(platformModule),
            appDeclaration = {
                androidContext(this@AppNameApplication)
            },
        )
    }
}
