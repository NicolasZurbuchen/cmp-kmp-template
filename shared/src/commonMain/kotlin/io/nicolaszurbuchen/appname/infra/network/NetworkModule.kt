package io.nicolaszurbuchen.appname.infra.network

import org.koin.dsl.module

val networkModule = module {
    single { createHttpClient() }
}
