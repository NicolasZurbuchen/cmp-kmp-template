package io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class PokemonApiImplTest {
    @Test
    fun getPokemon_hitsPokeApiEndpointWithGivenId() =
        runTest {
            var capturedUrl: String? = null
            val api = apiWithMockEngine(pikachuResponseJson) { request -> capturedUrl = request.url.toString() }

            api.getPokemon(id = 25)

            assertEquals("https://pokeapi.co/api/v2/pokemon/25", capturedUrl)
        }

    @Test
    fun getPokemon_deserializesResponseBodyIntoDto() =
        runTest {
            val api = apiWithMockEngine(pikachuResponseJson)

            val result = api.getPokemon(id = 25)

            assertEquals(25, result.id)
            assertEquals("pikachu", result.name)
            assertEquals(4, result.height)
            assertEquals(60, result.weight)
            assertEquals("https://example.com/pikachu.png", result.sprites.frontDefault)
        }

    @Test
    fun getPokemon_missingFrontDefaultSprite_mapsToNull() =
        runTest {
            val api = apiWithMockEngine(noSpriteResponseJson)

            val result = api.getPokemon(id = 1)

            assertEquals(null, result.sprites.frontDefault)
        }

    private fun apiWithMockEngine(
        responseBody: String,
        onRequest: (HttpRequestData) -> Unit = {},
    ): PokemonApi {
        val engine =
            MockEngine { request ->
                onRequest(request)
                respond(
                    content = responseBody,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }
        val client =
            HttpClient(engine) {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            }
        return PokemonApiImpl(client)
    }

    private val pikachuResponseJson =
        """
        {"id":25,"name":"pikachu","height":4,"weight":60,"sprites":{"front_default":"https://example.com/pikachu.png"}}
        """.trimIndent()

    private val noSpriteResponseJson =
        """
        {"id":1,"name":"bulbasaur","height":7,"weight":69,"sprites":{"front_default":null}}
        """.trimIndent()
}
