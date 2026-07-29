package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.main

import io.nicolaszurbuchen.appname.common.error.AppError
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.model.Pokemon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MainReducerTest {

    private val reducer = MainStoreFactory.ReducerImpl

    @Test
    fun generationStarted_setsLoadingTrueAndClearsError() {
        val state = MainState(isLoading = false, error = AppError.Network.Unavailable)

        val result = with(reducer) { state.reduce(MainMessage.GenerationStarted) }

        assertEquals(true, result.isLoading)
        assertNull(result.error)
    }

    @Test
    fun historyUpdated_setsHistoryAndClearsLoading() {
        val state = MainState(isLoading = true, error = AppError.Network.Unavailable)
        val items = listOf(samplePokemon())

        val result = with(reducer) { state.reduce(MainMessage.HistoryUpdated(items)) }

        assertEquals(false, result.isLoading)
        assertEquals(items, result.history)
        assertEquals(AppError.Network.Unavailable, result.error)
    }

    @Test
    fun generationFailed_setsErrorAndClearsLoading() {
        val state = MainState(isLoading = true)

        val result = with(reducer) {
            state.reduce(MainMessage.GenerationFailed(AppError.Network.Unavailable))
        }

        assertEquals(false, result.isLoading)
        assertEquals(AppError.Network.Unavailable, result.error)
    }

    @Test
    fun errorDismissed_clearsErrorOnly() {
        val history = listOf(samplePokemon())
        val state = MainState(isLoading = true, error = AppError.Network.Unavailable, history = history)

        val result = with(reducer) { state.reduce(MainMessage.ErrorDismissed) }

        assertNull(result.error)
        assertEquals(true, result.isLoading)
        assertEquals(history, result.history)
    }

    private fun samplePokemon() = Pokemon(
        historyId = 1L,
        speciesId = 25,
        name = "pikachu",
        spriteUrl = "https://example.com/pikachu.png",
        height = 4,
        weight = 60,
        fetchedAt = 1_000L,
    )
}
