package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote

import io.nicolaszurbuchen.appname.common.error.AppError
import io.nicolaszurbuchen.appname.common.error.AppException
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.api.NumberFactApi
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.mapper.toValue

class NumberFactRemoteDataSourceImpl(
    private val api: NumberFactApi,
) : NumberFactRemoteDataSource {
    override suspend fun fetchFact(number: Int): String? =
        try {
            api.getFact(number).toValue()
        } catch (_: Exception) {
            throw AppException(AppError.NumberGenerator.FactFetchFailed)
        }
}
