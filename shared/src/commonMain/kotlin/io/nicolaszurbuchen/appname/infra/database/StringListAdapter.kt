package io.nicolaszurbuchen.appname.infra.database

import app.cash.sqldelight.ColumnAdapter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

val stringListAdapter =
    object : ColumnAdapter<List<String>, String> {
        override fun decode(databaseValue: String): List<String> = Json.decodeFromString(ListSerializer(String.serializer()), databaseValue)

        override fun encode(value: List<String>): String = Json.encodeToString(ListSerializer(String.serializer()), value)
    }
