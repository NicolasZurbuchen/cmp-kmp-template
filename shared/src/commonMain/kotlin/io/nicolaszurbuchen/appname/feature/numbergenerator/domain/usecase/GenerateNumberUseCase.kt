package io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase

import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.model.GeneratedNumber
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.repository.NumberGeneratorRepository

class GenerateNumberUseCase(
    private val repository: NumberGeneratorRepository,
) {
    suspend operator fun invoke(): GeneratedNumber = repository.generateAndSave()
}
