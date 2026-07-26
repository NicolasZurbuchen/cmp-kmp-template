package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote

interface NumberFactRemoteDataSource {
    suspend fun fetchFact(number: Int): String?
}
