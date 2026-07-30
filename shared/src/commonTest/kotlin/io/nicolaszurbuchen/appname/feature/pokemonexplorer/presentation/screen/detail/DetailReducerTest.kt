package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.detail

import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.model.Pokemon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DetailReducerTest {
    private val reducer = DetailStoreFactory.ReducerImpl

    @Test
    fun pokemonLoaded_setsPokemonAndClearsLoading() {
        val state = DetailState(isLoading = true)
        val pokemon = samplePokemon()

        val result = with(reducer) { state.reduce(DetailMessage.PokemonLoaded(pokemon)) }

        assertEquals(false, result.isLoading)
        assertEquals(pokemon, result.pokemon)
    }

    @Test
    fun pokemonLoaded_nullPokemon_clearsLoadingWithNullPokemon() {
        val state = DetailState(isLoading = true)

        val result = with(reducer) { state.reduce(DetailMessage.PokemonLoaded(null)) }

        assertEquals(false, result.isLoading)
        assertNull(result.pokemon)
    }

    private fun samplePokemon() =
        Pokemon(
            historyId = 1L,
            speciesId = 25,
            name = "pikachu",
            spriteUrl = "https://example.com/pikachu.png",
            height = 4,
            weight = 60,
            fetchedAt = 1_000L,
        )
}
