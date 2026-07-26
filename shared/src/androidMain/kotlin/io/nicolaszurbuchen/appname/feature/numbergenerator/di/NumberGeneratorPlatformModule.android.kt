package io.nicolaszurbuchen.appname.feature.numbergenerator.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local.ConnectivityChecker
import org.koin.core.module.Module
import org.koin.dsl.module

private class AndroidConnectivityChecker(
    private val context: Context,
) : ConnectivityChecker {
    override fun isConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

actual val numberGeneratorPlatformModule: Module =
    module {
        single<ConnectivityChecker> { AndroidConnectivityChecker(get()) }
    }
