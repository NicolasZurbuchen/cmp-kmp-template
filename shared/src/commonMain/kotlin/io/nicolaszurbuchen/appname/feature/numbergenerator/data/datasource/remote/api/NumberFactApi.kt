package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.api

import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.dto.NumberFactDto

interface NumberFactApi {
    suspend fun getFact(number: Int): NumberFactDto
}
