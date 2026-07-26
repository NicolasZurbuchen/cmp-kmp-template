package io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase

import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.model.GeneratedNumber
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.repository.NumberGeneratorRepository
import kotlinx.coroutines.flow.Flow

class ObserveHistoryUseCase(
    private val repository: NumberGeneratorRepository,
) {
    operator fun invoke(): Flow<List<GeneratedNumber>> = repository.observeHistory()
}
