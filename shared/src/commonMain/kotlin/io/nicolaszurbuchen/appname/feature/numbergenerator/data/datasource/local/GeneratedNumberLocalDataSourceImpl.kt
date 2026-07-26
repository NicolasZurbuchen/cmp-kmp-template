package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class GeneratedNumberLocalDataSourceImpl(
    private val queries: GeneratedNumberQueries,
) : GeneratedNumberLocalDataSource {
    override suspend fun insert(
        value: Int,
        fact: String?,
        createdAt: Long,
        isSynced: Boolean,
    ): Long =
        queries.transactionWithResult {
            queries.insertGeneratedNumber(
                value_ = value.toLong(),
                fact = fact,
                created_at = createdAt,
                is_favorite = 0L,
                is_synced = if (isSynced) 1L else 0L,
            )
            queries.lastInsertRowId().executeAsOne()
        }

    override fun observeAll(): Flow<List<GeneratedNumberEntity>> =
        queries.selectAllOrderByCreatedAtDesc().asFlow().mapToList(Dispatchers.Default)

    override suspend fun getById(id: Long): GeneratedNumberEntity? = queries.selectById(id).executeAsOneOrNull()

    override suspend fun setFavorite(
        id: Long,
        isFavorite: Boolean,
    ) {
        queries.updateFavorite(is_favorite = if (isFavorite) 1L else 0L, id = id)
    }

    override suspend fun updateFact(
        id: Long,
        fact: String,
        isSynced: Boolean,
    ) {
        queries.updateFact(fact = fact, is_synced = if (isSynced) 1L else 0L, id = id)
    }

    override suspend fun getUnsynced(): List<GeneratedNumberEntity> = queries.selectUnsynced().executeAsList()
}
