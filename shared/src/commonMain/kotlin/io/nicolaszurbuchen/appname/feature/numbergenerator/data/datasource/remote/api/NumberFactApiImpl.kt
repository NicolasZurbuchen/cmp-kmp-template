package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.dto.NumberFactDto

class NumberFactApiImpl(
    private val client: HttpClient,
) : NumberFactApi {
    override suspend fun getFact(number: Int): NumberFactDto =
        client.get("https://numbersapi.com/$number") {
            parameter("json", true)
        }.body()
}
