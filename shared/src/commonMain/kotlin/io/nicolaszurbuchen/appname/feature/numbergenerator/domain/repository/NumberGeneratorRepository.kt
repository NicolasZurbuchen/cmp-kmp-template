package io.nicolaszurbuchen.appname.feature.numbergenerator.domain.repository

import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.model.GeneratedNumber
import kotlinx.coroutines.flow.Flow

interface NumberGeneratorRepository {
    suspend fun generateAndSave(): GeneratedNumber

    fun observeHistory(): Flow<List<GeneratedNumber>>

    suspend fun toggleFavorite(id: Long)

    suspend fun syncPending()
}
