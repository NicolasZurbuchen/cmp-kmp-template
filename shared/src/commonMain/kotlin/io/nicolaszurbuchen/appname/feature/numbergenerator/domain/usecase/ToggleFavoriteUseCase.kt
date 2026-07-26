package io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase

import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.repository.NumberGeneratorRepository

class ToggleFavoriteUseCase(
    private val repository: NumberGeneratorRepository,
) {
    suspend operator fun invoke(id: Long) = repository.toggleFavorite(id)
}
