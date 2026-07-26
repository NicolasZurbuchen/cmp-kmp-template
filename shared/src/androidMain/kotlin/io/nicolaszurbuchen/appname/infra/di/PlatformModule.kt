package io.nicolaszurbuchen.appname.infra.di

import io.nicolaszurbuchen.appname.infra.database.DatabaseDriverFactory
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val platformModule =
    module {
        singleOf(::DatabaseDriverFactory)
    }
