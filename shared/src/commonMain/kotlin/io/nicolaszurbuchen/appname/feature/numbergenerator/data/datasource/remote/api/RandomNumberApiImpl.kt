package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText

class RandomNumberApiImpl(
    private val client: HttpClient,
) : RandomNumberApi {
    override suspend fun getRandomNumber(
        min: Int,
        max: Int,
    ): Int {
        val response =
            client.get("https://www.random.org/integers/") {
                parameter("num", 1)
                parameter("min", min)
                parameter("max", max)
                parameter("col", 1)
                parameter("base", 10)
                parameter("format", "plain")
            }.bodyAsText()
        return response.trim().toInt()
    }
}
