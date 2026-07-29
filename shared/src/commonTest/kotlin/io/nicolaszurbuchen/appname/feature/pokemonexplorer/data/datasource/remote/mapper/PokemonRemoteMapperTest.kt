package io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.mapper

import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.dto.PokemonDto
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.dto.PokemonSpritesDto
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.model.Pokemon
import kotlin.test.Test
import kotlin.test.assertEquals

class PokemonRemoteMapperTest {

    @Test
    fun toDomain_mapsIdToSpeciesIdAndKeepsHistoryIdAndFetchedAtFromParameters() {
        val dto = PokemonDto(id = 25, name = "pikachu", height = 4, weight = 60, sprites = PokemonSpritesDto(frontDefault = "sprite-url"))

        val result = dto.toDomain(historyId = 7L, fetchedAt = 123L)

        assertEquals(
            Pokemon(historyId = 7L, speciesId = 25, name = "pikachu", spriteUrl = "sprite-url", height = 4, weight = 60, fetchedAt = 123L),
            result,
        )
    }

    @Test
    fun toDomain_nullFrontDefaultSprite_mapsToEmptyString() {
        val dto = PokemonDto(id = 1, name = "bulbasaur", height = 7, weight = 69, sprites = PokemonSpritesDto(frontDefault = null))

        val result = dto.toDomain(historyId = 1L, fetchedAt = 0L)

        assertEquals("", result.spriteUrl)
    }
}
