package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local

import kotlinx.coroutines.flow.Flow

interface GeneratedNumberLocalDataSource {
    suspend fun insert(
        value: Int,
        fact: String?,
        createdAt: Long,
        isSynced: Boolean,
    ): Long

    fun observeAll(): Flow<List<GeneratedNumberEntity>>

    suspend fun getById(id: Long): GeneratedNumberEntity?

    suspend fun setFavorite(
        id: Long,
        isFavorite: Boolean,
    )

    suspend fun updateFact(
        id: Long,
        fact: String,
        isSynced: Boolean,
    )

    suspend fun getUnsynced(): List<GeneratedNumberEntity>
}
