package io.nicolaszurbuchen.appname.feature.numbergenerator.data.repository

import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local.GeneratedNumberLocalDataSource
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local.mapper.toDomain
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.NumberFactRemoteDataSource
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.RandomNumberRemoteDataSource
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.platform.ConnectivityChecker
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.model.GeneratedNumber
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.repository.NumberGeneratorRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val MIN_VALUE = 1
private const val MAX_VALUE = 100

class NumberGeneratorRepositoryImpl(
    private val randomNumberRemoteDataSource: RandomNumberRemoteDataSource,
    private val numberFactRemoteDataSource: NumberFactRemoteDataSource,
    private val localDataSource: GeneratedNumberLocalDataSource,
    private val connectivityChecker: ConnectivityChecker,
) : NumberGeneratorRepository {
    @OptIn(ExperimentalTime::class)
    override suspend fun generateAndSave(): GeneratedNumber {
        val value = randomNumberRemoteDataSource.fetchRandomNumber(MIN_VALUE, MAX_VALUE)

        val fact =
            if (connectivityChecker.isConnected()) {
                try {
                    numberFactRemoteDataSource.fetchFact(value)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    null
                }
            } else {
                null
            }

        val createdAt = Clock.System.now().toEpochMilliseconds()
        val isSynced = fact != null
        val id = localDataSource.insert(value = value, fact = fact, createdAt = createdAt, isSynced = isSynced)

        return GeneratedNumber(
            id = id,
            value = value,
            fact = fact,
            createdAt = createdAt,
            isFavorite = false,
            isSynced = isSynced,
        )
    }

    override fun observeHistory(): Flow<List<GeneratedNumber>> = localDataSource.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun toggleFavorite(id: Long) {
        // observeHistory() reflects the change automatically once the row is updated.
        val current = localDataSource.getById(id) ?: return
        localDataSource.setFavorite(id, current.is_favorite != 1L)
    }

    override suspend fun syncPending() {
        localDataSource.getUnsynced().forEach { row ->
            try {
                val fact = numberFactRemoteDataSource.fetchFact(row.value_.toInt())
                if (fact != null) {
                    localDataSource.updateFact(row.id, fact, isSynced = true)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Leave this row unsynced; the next manual sync will retry it.
            }
        }
    }
}
