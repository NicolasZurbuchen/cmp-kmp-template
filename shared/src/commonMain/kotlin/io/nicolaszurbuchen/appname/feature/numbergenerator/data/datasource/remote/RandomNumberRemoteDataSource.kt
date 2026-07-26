package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote

interface RandomNumberRemoteDataSource {
    suspend fun fetchRandomNumber(
        min: Int,
        max: Int,
    ): Int
}
