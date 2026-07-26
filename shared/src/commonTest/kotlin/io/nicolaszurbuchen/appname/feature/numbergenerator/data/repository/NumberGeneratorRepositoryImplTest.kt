package io.nicolaszurbuchen.appname.feature.numbergenerator.data.repository

import io.nicolaszurbuchen.appname.common.error.AppError
import io.nicolaszurbuchen.appname.common.error.AppException
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local.GeneratedNumberEntity
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local.GeneratedNumberLocalDataSource
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.NumberFactRemoteDataSource
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.RandomNumberRemoteDataSource
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.platform.ConnectivityChecker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeRandomNumberRemoteDataSource(
    private val result: Int? = null,
    private val failure: Exception? = null,
) : RandomNumberRemoteDataSource {
    override suspend fun fetchRandomNumber(
        min: Int,
        max: Int,
    ): Int = failure?.let { throw it } ?: result!!
}

private class FakeNumberFactRemoteDataSource(
    private val result: String? = null,
    private val failure: Exception? = null,
    private val failuresByNumber: Map<Int, Exception> = emptyMap(),
    private val resultsByNumber: Map<Int, String?> = emptyMap(),
) : NumberFactRemoteDataSource {
    override suspend fun fetchFact(number: Int): String? {
        failuresByNumber[number]?.let { throw it }
        if (resultsByNumber.containsKey(number)) return resultsByNumber.getValue(number)
        return failure?.let { throw it } ?: result
    }
}

private class FakeConnectivityChecker(
    private val connected: Boolean,
) : ConnectivityChecker {
    override fun isConnected(): Boolean = connected
}

private class FakeGeneratedNumberLocalDataSource : GeneratedNumberLocalDataSource {
    var lastInsert: Triple<Int, String?, Boolean>? = null
    var rows: MutableList<GeneratedNumberEntity> = mutableListOf()
    var lastSetFavorite: Pair<Long, Boolean>? = null
    val updateFactCalls: MutableList<Triple<Long, String, Boolean>> = mutableListOf()

    override suspend fun insert(
        value: Int,
        fact: String?,
        createdAt: Long,
        isSynced: Boolean,
    ): Long {
        lastInsert = Triple(value, fact, isSynced)
        return 1L
    }

    override fun observeAll(): Flow<List<GeneratedNumberEntity>> = flowOf(emptyList())

    override suspend fun getById(id: Long): GeneratedNumberEntity? = rows.find { it.id == id }

    override suspend fun setFavorite(
        id: Long,
        isFavorite: Boolean,
    ) {
        lastSetFavorite = id to isFavorite
    }

    override suspend fun updateFact(
        id: Long,
        fact: String,
        isSynced: Boolean,
    ) {
        updateFactCalls.add(Triple(id, fact, isSynced))
        val index = rows.indexOfFirst { it.id == id }
        if (index != -1) {
            rows[index] = rows[index].copy(fact = fact, is_synced = if (isSynced) 1L else 0L)
        }
    }

    override suspend fun getUnsynced(): List<GeneratedNumberEntity> = rows.filter { it.is_synced == 0L }
}

class NumberGeneratorRepositoryImplTest {
    @Test
    fun `generateAndSave persists both fields when online and the fact call succeeds`() =
        runTest {
            val local = FakeGeneratedNumberLocalDataSource()
            val repository =
                NumberGeneratorRepositoryImpl(
                    randomNumberRemoteDataSource = FakeRandomNumberRemoteDataSource(result = 42),
                    numberFactRemoteDataSource = FakeNumberFactRemoteDataSource(result = "42 is nice"),
                    localDataSource = local,
                    connectivityChecker = FakeConnectivityChecker(connected = true),
                )

            val result = repository.generateAndSave()

            assertEquals(42, result.value)
            assertEquals("42 is nice", result.fact)
            assertTrue(result.isSynced)
            assertEquals(Triple(42, "42 is nice", true), local.lastInsert)
        }

    @Test
    fun `generateAndSave persists with a null fact when offline`() =
        runTest {
            val local = FakeGeneratedNumberLocalDataSource()
            val repository =
                NumberGeneratorRepositoryImpl(
                    randomNumberRemoteDataSource = FakeRandomNumberRemoteDataSource(result = 7),
                    numberFactRemoteDataSource = FakeNumberFactRemoteDataSource(result = "unused"),
                    localDataSource = local,
                    connectivityChecker = FakeConnectivityChecker(connected = false),
                )

            val result = repository.generateAndSave()

            assertEquals(7, result.value)
            assertEquals(null, result.fact)
            assertFalse(result.isSynced)
            assertEquals(Triple(7, null, false), local.lastInsert)
        }

    @Test
    fun `generateAndSave persists with a null fact when the fact call fails`() =
        runTest {
            val local = FakeGeneratedNumberLocalDataSource()
            val repository =
                NumberGeneratorRepositoryImpl(
                    randomNumberRemoteDataSource = FakeRandomNumberRemoteDataSource(result = 13),
                    numberFactRemoteDataSource =
                        FakeNumberFactRemoteDataSource(failure = AppException(AppError.NumberGenerator.FactFetchFailed)),
                    localDataSource = local,
                    connectivityChecker = FakeConnectivityChecker(connected = true),
                )

            val result = repository.generateAndSave()

            assertEquals(13, result.value)
            assertEquals(null, result.fact)
            assertFalse(result.isSynced)
        }

    @Test
    fun `generateAndSave throws and persists nothing when the number call fails`() =
        runTest {
            val local = FakeGeneratedNumberLocalDataSource()
            val repository =
                NumberGeneratorRepositoryImpl(
                    randomNumberRemoteDataSource =
                        FakeRandomNumberRemoteDataSource(failure = AppException(AppError.NumberGenerator.NumberFetchFailed)),
                    numberFactRemoteDataSource = FakeNumberFactRemoteDataSource(result = "unused"),
                    localDataSource = local,
                    connectivityChecker = FakeConnectivityChecker(connected = true),
                )

            val exception = assertFailsWith<AppException> { repository.generateAndSave() }

            assertEquals(AppError.NumberGenerator.NumberFetchFailed, exception.error)
            assertEquals(null, local.lastInsert)
        }

    @Test
    fun `toggleFavorite flips the current favorite state of the row`() =
        runTest {
            val local =
                FakeGeneratedNumberLocalDataSource().apply {
                    rows.add(GeneratedNumberEntity(5L, 9L, "fact", 100L, 0L, 1L))
                }
            val repository =
                NumberGeneratorRepositoryImpl(
                    randomNumberRemoteDataSource = FakeRandomNumberRemoteDataSource(result = 1),
                    numberFactRemoteDataSource = FakeNumberFactRemoteDataSource(result = "unused"),
                    localDataSource = local,
                    connectivityChecker = FakeConnectivityChecker(connected = true),
                )

            repository.toggleFavorite(5L)

            assertEquals(5L to true, local.lastSetFavorite)
        }

    @Test
    fun `toggleFavorite does nothing when the row no longer exists`() =
        runTest {
            val local = FakeGeneratedNumberLocalDataSource()
            val repository =
                NumberGeneratorRepositoryImpl(
                    randomNumberRemoteDataSource = FakeRandomNumberRemoteDataSource(result = 1),
                    numberFactRemoteDataSource = FakeNumberFactRemoteDataSource(result = "unused"),
                    localDataSource = local,
                    connectivityChecker = FakeConnectivityChecker(connected = true),
                )

            repository.toggleFavorite(999L)

            assertEquals(null, local.lastSetFavorite)
        }

    @Test
    fun `syncPending updates rows whose fact fetch succeeds`() =
        runTest {
            val local =
                FakeGeneratedNumberLocalDataSource().apply {
                    rows.add(GeneratedNumberEntity(1L, 42L, null, 100L, 0L, 0L))
                }
            val repository =
                NumberGeneratorRepositoryImpl(
                    randomNumberRemoteDataSource = FakeRandomNumberRemoteDataSource(result = 1),
                    numberFactRemoteDataSource = FakeNumberFactRemoteDataSource(result = "42 is nice"),
                    localDataSource = local,
                    connectivityChecker = FakeConnectivityChecker(connected = true),
                )

            repository.syncPending()

            assertEquals(listOf(Triple(1L, "42 is nice", true)), local.updateFactCalls)
            assertEquals("42 is nice", local.rows.single().fact)
            assertEquals(1L, local.rows.single().is_synced)
        }

    @Test
    fun `syncPending leaves a row unsynced when its fact fetch fails`() =
        runTest {
            val local =
                FakeGeneratedNumberLocalDataSource().apply {
                    rows.add(GeneratedNumberEntity(1L, 42L, null, 100L, 0L, 0L))
                }
            val repository =
                NumberGeneratorRepositoryImpl(
                    randomNumberRemoteDataSource = FakeRandomNumberRemoteDataSource(result = 1),
                    numberFactRemoteDataSource =
                        FakeNumberFactRemoteDataSource(failure = AppException(AppError.NumberGenerator.FactFetchFailed)),
                    localDataSource = local,
                    connectivityChecker = FakeConnectivityChecker(connected = true),
                )

            repository.syncPending()

            assertTrue(local.updateFactCalls.isEmpty())
            assertEquals(null, local.rows.single().fact)
            assertEquals(0L, local.rows.single().is_synced)
        }

    @Test
    fun `syncPending does not throw when a row's fact fetch fails`() =
        runTest {
            val local =
                FakeGeneratedNumberLocalDataSource().apply {
                    rows.add(GeneratedNumberEntity(1L, 42L, null, 100L, 0L, 0L))
                }
            val repository =
                NumberGeneratorRepositoryImpl(
                    randomNumberRemoteDataSource = FakeRandomNumberRemoteDataSource(result = 1),
                    numberFactRemoteDataSource =
                        FakeNumberFactRemoteDataSource(failure = AppException(AppError.NumberGenerator.FactFetchFailed)),
                    localDataSource = local,
                    connectivityChecker = FakeConnectivityChecker(connected = true),
                )

            repository.syncPending()
        }

    @Test
    fun `syncPending syncs the row that succeeds even when another row in the same call fails`() =
        runTest {
            val local =
                FakeGeneratedNumberLocalDataSource().apply {
                    rows.add(GeneratedNumberEntity(1L, 13L, null, 100L, 0L, 0L))
                    rows.add(GeneratedNumberEntity(2L, 42L, null, 200L, 0L, 0L))
                }
            val repository =
                NumberGeneratorRepositoryImpl(
                    randomNumberRemoteDataSource = FakeRandomNumberRemoteDataSource(result = 1),
                    numberFactRemoteDataSource =
                        FakeNumberFactRemoteDataSource(
                            failuresByNumber = mapOf(13 to AppException(AppError.NumberGenerator.FactFetchFailed)),
                            resultsByNumber = mapOf(42 to "42 is nice"),
                        ),
                    localDataSource = local,
                    connectivityChecker = FakeConnectivityChecker(connected = true),
                )

            repository.syncPending()

            assertEquals(listOf(Triple(2L, "42 is nice", true)), local.updateFactCalls)
            assertEquals(0L, local.rows.single { it.id == 1L }.is_synced)
            assertEquals(1L, local.rows.single { it.id == 2L }.is_synced)
            assertEquals("42 is nice", local.rows.single { it.id == 2L }.fact)
        }
}
