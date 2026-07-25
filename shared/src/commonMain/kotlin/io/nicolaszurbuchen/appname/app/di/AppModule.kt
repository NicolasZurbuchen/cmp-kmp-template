package io.nicolaszurbuchen.appname.app.di

import io.nicolaszurbuchen.appname.infra.mvi.storeModule
import io.nicolaszurbuchen.appname.infra.network.networkModule

val appModule = listOf(
    storeModule,
    networkModule,
)
