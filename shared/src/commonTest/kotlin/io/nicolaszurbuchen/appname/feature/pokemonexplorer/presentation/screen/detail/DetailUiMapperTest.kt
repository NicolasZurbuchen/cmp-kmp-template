package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.detail

import appname.shared.generated.resources.Res
import appname.shared.generated.resources.pokemon_detail_height_label
import appname.shared.generated.resources.pokemon_detail_weight_label
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.model.Pokemon
import io.nicolaszurbuchen.appname.infra.ui.UiText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DetailUiMapperTest {

    @Test
    fun toUiModel_loadingWithNullPokemon_producesAllNullFields() {
        val state = DetailState(isLoading = true, pokemon = null)

        val result = state.toUiModel()

        assertEquals(true, result.isLoading)
        assertNull(result.numberText)
        assertNull(result.name)
        assertNull(result.spriteUrl)
        assertNull(result.heightText)
        assertNull(result.weightText)
    }

    @Test
    fun toUiModel_numberTextPadsSpeciesIdToThreeDigits() {
        val state = DetailState(isLoading = false, pokemon = samplePokemon(speciesId = 7))

        val result = state.toUiModel()

        assertEquals("#007", result.numberText)
    }

    @Test
    fun toUiModel_nameIsCapitalized() {
        val state = DetailState(isLoading = false, pokemon = samplePokemon(name = "pikachu"))

        val result = state.toUiModel()

        assertEquals("Pikachu", result.name)
    }

    @Test
    fun toUiModel_spriteUrlPassedThroughUnchanged() {
        val state = DetailState(isLoading = false, pokemon = samplePokemon(spriteUrl = "https://example.com/x.png"))

        val result = state.toUiModel()

        assertEquals("https://example.com/x.png", result.spriteUrl)
    }

    @Test
    fun toUiModel_heightTextFormatsDecimetersAsMeters() {
        val state = DetailState(isLoading = false, pokemon = samplePokemon(height = 4))

        val result = state.toUiModel()

        assertEquals(
            UiText.Resource(Res.string.pokemon_detail_height_label, listOf("0.4 m")),
            result.heightText,
        )
    }

    @Test
    fun toUiModel_weightTextFormatsHectogramsAsKilograms() {
        val state = DetailState(isLoading = false, pokemon = samplePokemon(weight = 60))

        val result = state.toUiModel()

        assertEquals(
            UiText.Resource(Res.string.pokemon_detail_weight_label, listOf("6.0 kg")),
            result.weightText,
        )
    }

    private fun samplePokemon(
        speciesId: Int = 25,
        name: String = "pikachu",
        spriteUrl: String = "https://example.com/pikachu.png",
        height: Int = 4,
        weight: Int = 60,
    ) = Pokemon(
        historyId = 1L,
        speciesId = speciesId,
        name = name,
        spriteUrl = spriteUrl,
        height = height,
        weight = weight,
        fetchedAt = 1_000L,
    )
}
