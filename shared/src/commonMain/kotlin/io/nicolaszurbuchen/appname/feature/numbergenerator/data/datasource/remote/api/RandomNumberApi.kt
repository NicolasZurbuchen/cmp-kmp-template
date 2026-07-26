package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.api

interface RandomNumberApi {
    suspend fun getRandomNumber(
        min: Int,
        max: Int,
    ): Int
}
