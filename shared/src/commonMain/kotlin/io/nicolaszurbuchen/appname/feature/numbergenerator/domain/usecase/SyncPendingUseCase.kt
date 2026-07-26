package io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase

import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.repository.NumberGeneratorRepository

class SyncPendingUseCase(
    private val repository: NumberGeneratorRepository,
) {
    suspend operator fun invoke() = repository.syncPending()
}
