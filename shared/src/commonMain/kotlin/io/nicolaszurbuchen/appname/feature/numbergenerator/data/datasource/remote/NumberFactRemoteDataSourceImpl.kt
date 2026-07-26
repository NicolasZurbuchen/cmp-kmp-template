package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote

import io.nicolaszurbuchen.appname.common.error.AppError
import io.nicolaszurbuchen.appname.common.error.AppException
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.api.NumberFactApi
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.mapper.toValue
import kotlinx.coroutines.CancellationException

class NumberFactRemoteDataSourceImpl(
    private val api: NumberFactApi,
) : NumberFactRemoteDataSource {
    override suspend fun fetchFact(number: Int): String? =
        try {
            api.getFact(number).toValue()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            throw AppException(AppError.NumberGenerator.FactFetchFailed)
        }
}
