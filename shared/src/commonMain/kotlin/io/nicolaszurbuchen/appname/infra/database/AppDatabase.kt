package io.nicolaszurbuchen.appname.infra.database

import io.nicolaszurbuchen.appname.cache.AppDatabase

fun createDatabase(driverFactory: DatabaseDriverFactory): AppDatabase {
    val driver = driverFactory.createDriver()
    return AppDatabase(
        driver = driver,
    )
}
