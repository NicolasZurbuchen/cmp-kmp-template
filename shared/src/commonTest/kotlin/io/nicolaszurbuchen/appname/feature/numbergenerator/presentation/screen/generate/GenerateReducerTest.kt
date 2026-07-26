package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate

import io.nicolaszurbuchen.appname.common.error.AppError
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.model.GeneratedNumber
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GenerateReducerTest {
    private fun reduce(
        state: GenerateState,
        message: GenerateMessage,
    ): GenerateState = with(GenerateReducer) { state.reduce(message) }

    @Test
    fun `GenerationStarted sets isLoading and clears error`() {
        val result = reduce(GenerateState(error = AppError.NumberGenerator.NumberFetchFailed), GenerateMessage.GenerationStarted)

        assertTrue(result.isLoading)
        assertNull(result.error)
    }

    @Test
    fun `GenerationSucceeded stores the number and clears loading and error`() {
        val number = GeneratedNumber(1, 42, "a fact", 1000L, false, true)

        val result = reduce(GenerateState(isLoading = true), GenerateMessage.GenerationSucceeded(number))

        assertEquals(number, result.lastGenerated)
        assertEquals(false, result.isLoading)
        assertNull(result.error)
    }

    @Test
    fun `GenerationPartiallyFailed stores the number and sets FactFetchFailed as a non-blocking error`() {
        val number = GeneratedNumber(2, 7, null, 2000L, false, false)

        val result = reduce(GenerateState(isLoading = true), GenerateMessage.GenerationPartiallyFailed(number))

        assertEquals(number, result.lastGenerated)
        assertEquals(false, result.isLoading)
        assertEquals(AppError.NumberGenerator.FactFetchFailed, result.error)
    }

    @Test
    fun `GenerationFailed leaves lastGenerated untouched and sets the error`() {
        val previous = GeneratedNumber(3, 5, "old fact", 500L, true, true)

        val result =
            reduce(
                GenerateState(isLoading = true, lastGenerated = previous),
                GenerateMessage.GenerationFailed(AppError.NumberGenerator.NumberFetchFailed),
            )

        assertEquals(previous, result.lastGenerated)
        assertEquals(false, result.isLoading)
        assertEquals(AppError.NumberGenerator.NumberFetchFailed, result.error)
    }
}
