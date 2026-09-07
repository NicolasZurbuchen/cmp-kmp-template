package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.main

import io.nicolaszurbuchen.appname.core.error.AppError
import io.nicolaszurbuchen.appname.core.error.toUiModel
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.model.Pokemon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MainUiMapperTest {
    @Test
    fun toUiModel_passesThroughLoadingFlag() {
        val state = MainState(isLoading = true)

        val result = state.toUiModel()

        assertEquals(true, result.isLoading)
    }

    @Test
    fun toUiModel_nullError_mapsToNullError() {
        val state = MainState(error = null)

        val result = state.toUiModel()

        assertNull(result.error)
    }

    @Test
    fun toUiModel_nonNullError_mapsToNonNullError() {
        val state = MainState(error = AppError.Network.Unavailable)

        val result = state.toUiModel()

        assertEquals(AppError.Network.Unavailable.toUiModel(), result.error)
    }

    @Test
    fun toUiModel_emptyHistory_producesNullHeroAndEmptyHistory() {
        val state = MainState(history = emptyList())

        val result = state.toUiModel()

        assertNull(result.hero)
        assertEquals(emptyList(), result.history)
    }

    @Test
    fun toUiModel_firstHistoryItemBecomesHero() {
        val first = samplePokemon(historyId = 1L, speciesId = 25, name = "pikachu")
        val second = samplePokemon(historyId = 2L, speciesId = 1, name = "bulbasaur")
        val state = MainState(history = listOf(first, second))

        val result = state.toUiModel()

        assertEquals(1L, result.hero?.historyId)
        assertEquals(1, result.history.size)
        assertEquals(2L, result.history.first().historyId)
    }

    @Test
    fun toUiModel_numberTextPadsSpeciesIdToThreeDigits() {
        val state = MainState(history = listOf(samplePokemon(speciesId = 7)))

        val result = state.toUiModel()

        assertEquals("#007", result.hero?.numberText)
    }

    @Test
    fun toUiModel_nameIsCapitalized() {
        val state = MainState(history = listOf(samplePokemon(name = "pikachu")))

        val result = state.toUiModel()

        assertEquals("Pikachu", result.hero?.name)
    }

    @Test
    fun toUiModel_spriteUrlPassedThroughUnchanged() {
        val state = MainState(history = listOf(samplePokemon(spriteUrl = "https://example.com/x.png")))

        val result = state.toUiModel()

        assertEquals("https://example.com/x.png", result.hero?.spriteUrl)
    }

    private fun samplePokemon(
        historyId: Long = 1L,
        speciesId: Int = 25,
        name: String = "pikachu",
        spriteUrl: String = "https://example.com/pikachu.png",
    ) = Pokemon(
        historyId = historyId,
        speciesId = speciesId,
        name = name,
        spriteUrl = spriteUrl,
        height = 4,
        weight = 60,
        fetchedAt = 1_000L,
    )
}
