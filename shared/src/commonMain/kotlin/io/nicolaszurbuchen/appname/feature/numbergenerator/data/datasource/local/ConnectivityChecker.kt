package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local

interface ConnectivityChecker {
    fun isConnected(): Boolean
}
