package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history

import io.nicolaszurbuchen.appname.common.error.AppError
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.model.GeneratedNumber
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HistoryReducerTest {
    private fun reduce(
        state: HistoryState,
        message: HistoryMessage,
    ): HistoryState = with(HistoryReducer) { state.reduce(message) }

    @Test
    fun `HistoryUpdated replaces the items list`() {
        val items = listOf(GeneratedNumber(1, 42, "fact", 1000L, false, true))

        val result = reduce(HistoryState(), HistoryMessage.HistoryUpdated(items))

        assertEquals(items, result.items)
    }

    @Test
    fun `SyncStarted sets isSyncing and clears syncError`() {
        val result =
            reduce(HistoryState(syncError = AppError.NumberGenerator.FactFetchFailed), HistoryMessage.SyncStarted)

        assertTrue(result.isSyncing)
        assertNull(result.syncError)
    }

    @Test
    fun `SyncFinished clears isSyncing and stores any error`() {
        val result =
            reduce(
                HistoryState(isSyncing = true),
                HistoryMessage.SyncFinished(AppError.NumberGenerator.FactFetchFailed),
            )

        assertFalse(result.isSyncing)
        assertEquals(AppError.NumberGenerator.FactFetchFailed, result.syncError)
    }
}
