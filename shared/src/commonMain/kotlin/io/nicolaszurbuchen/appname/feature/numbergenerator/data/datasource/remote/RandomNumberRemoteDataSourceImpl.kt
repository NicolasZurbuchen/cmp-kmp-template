package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote

import io.nicolaszurbuchen.appname.common.error.AppError
import io.nicolaszurbuchen.appname.common.error.AppException
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.api.RandomNumberApi

class RandomNumberRemoteDataSourceImpl(
    private val api: RandomNumberApi,
) : RandomNumberRemoteDataSource {
    override suspend fun fetchRandomNumber(
        min: Int,
        max: Int,
    ): Int =
        try {
            api.getRandomNumber(min, max)
        } catch (_: Exception) {
            throw AppException(AppError.NumberGenerator.NumberFetchFailed)
        }
}
