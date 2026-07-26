package io.nicolaszurbuchen.appname.feature.numbergenerator.di

import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local.ConnectivityChecker
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.CoreFoundation.CFRelease
import platform.SystemConfiguration.SCNetworkReachabilityCreateWithName
import platform.SystemConfiguration.SCNetworkReachabilityFlagsVar
import platform.SystemConfiguration.SCNetworkReachabilityGetFlags
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsReachable

@OptIn(ExperimentalForeignApi::class)
private class IosConnectivityChecker : ConnectivityChecker {
    override fun isConnected(): Boolean =
        memScoped {
            val reachability = SCNetworkReachabilityCreateWithName(null, "www.random.org") ?: return false
            try {
                val flags = alloc<SCNetworkReachabilityFlagsVar>()
                val success = SCNetworkReachabilityGetFlags(reachability, flags.ptr)
                success && (flags.value and kSCNetworkReachabilityFlagsReachable) != 0u
            } finally {
                CFRelease(reachability)
            }
        }
}

actual val numberGeneratorPlatformModule: Module =
    module {
        single<ConnectivityChecker> { IosConnectivityChecker() }
    }
