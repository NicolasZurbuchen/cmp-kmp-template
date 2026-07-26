package io.nicolaszurbuchen.appname.infra.database

import org.koin.dsl.module

val databaseModule =
    module {
        single { createDatabase(get()) }
    }
