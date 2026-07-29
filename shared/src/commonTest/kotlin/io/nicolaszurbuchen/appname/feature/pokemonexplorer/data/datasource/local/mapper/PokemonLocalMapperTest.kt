package io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.local.mapper

import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.local.CachedPokemon
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.model.Pokemon
import kotlin.test.Test
import kotlin.test.assertEquals

class PokemonLocalMapperTest {
    @Test
    fun toDomain_mapsAllFieldsCorrectly() {
        val cached =
            CachedPokemon(
                id = 7L,
                pokemon_id = 25L,
                name = "pikachu",
                sprite_url = "https://example.com/pikachu.png",
                height = 4L,
                weight = 60L,
                fetched_at = 123456L,
            )

        val result = cached.toDomain()

        assertEquals(
            Pokemon(
                historyId = 7L,
                speciesId = 25,
                name = "pikachu",
                spriteUrl = "https://example.com/pikachu.png",
                height = 4,
                weight = 60,
                fetchedAt = 123456L,
            ),
            result,
        )
    }

    @Test
    fun toDomain_mapsHistoryIdFromRowIdNotSpeciesId() {
        val cached = CachedPokemon(id = 1L, pokemon_id = 999L, name = "x", sprite_url = "", height = 1L, weight = 1L, fetched_at = 0L)

        val result = cached.toDomain()

        assertEquals(1L, result.historyId)
        assertEquals(999, result.speciesId)
    }
}
