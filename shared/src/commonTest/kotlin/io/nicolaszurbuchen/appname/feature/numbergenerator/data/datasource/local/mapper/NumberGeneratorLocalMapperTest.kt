package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local.mapper

import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local.GeneratedNumberEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NumberGeneratorLocalMapperTest {
    @Test
    fun `toDomain maps a fully-synced row`() {
        val entity =
            GeneratedNumberEntity(
                id = 1L,
                value_ = 42L,
                fact = "42 is the answer",
                created_at = 1_700_000_000_000L,
                is_favorite = 1L,
                is_synced = 1L,
            )

        val domain = entity.toDomain()

        assertEquals(1L, domain.id)
        assertEquals(42, domain.value)
        assertEquals("42 is the answer", domain.fact)
        assertEquals(1_700_000_000_000L, domain.createdAt)
        assertTrue(domain.isFavorite)
        assertTrue(domain.isSynced)
    }

    @Test
    fun `toDomain maps an unsynced row with no fact`() {
        val entity =
            GeneratedNumberEntity(
                id = 2L,
                value_ = 7L,
                fact = null,
                created_at = 1_700_000_001_000L,
                is_favorite = 0L,
                is_synced = 0L,
            )

        val domain = entity.toDomain()

        assertEquals(null, domain.fact)
        assertFalse(domain.isFavorite)
        assertFalse(domain.isSynced)
    }
}
