package io.nicolaszurbuchen.appname

import android.app.Application
import io.nicolaszurbuchen.appname.app.di.initKoin
import org.koin.android.ext.koin.androidContext

class AppNameApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(
            appDeclaration = {
                androidContext(this@AppNameApplication)
            }
        )
    }
}
