package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.mapper

import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.dto.NumberFactDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NumberGeneratorRemoteMapperTest {
    @Test
    fun `toValue returns the fact text when found is true`() {
        val dto = NumberFactDto(text = "42 is the answer", number = 42, found = true, type = "trivia")

        assertEquals("42 is the answer", dto.toValue())
    }

    @Test
    fun `toValue returns null when found is false`() {
        val dto = NumberFactDto(text = "42 is a number.", number = 42, found = false, type = "trivia")

        assertNull(dto.toValue())
    }
}
