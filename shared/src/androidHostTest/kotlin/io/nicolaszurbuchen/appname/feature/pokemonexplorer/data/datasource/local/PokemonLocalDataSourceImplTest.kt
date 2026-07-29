package io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import io.nicolaszurbuchen.appname.cache.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PokemonLocalDataSourceImplTest {

    private lateinit var driver: SqlDriver
    private lateinit var dataSource: PokemonLocalDataSourceImpl

    @BeforeTest
    fun setup() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        AppDatabase.Schema.create(driver)
        val database = AppDatabase(driver = driver)
        dataSource = PokemonLocalDataSourceImpl(queries = database.cachedPokemonQueries)
    }

    @AfterTest
    fun tearDown() {
        driver.close()
    }

    // region insert / getById

    @Test
    fun insert_thenGetById_roundTripsCorrectly() = runTest {
        val id = dataSource.insert(
            pokemonId = 25,
            name = "pikachu",
            spriteUrl = "https://example.com/pikachu.png",
            height = 4,
            weight = 60,
            fetchedAt = 123L,
        )

        val result = dataSource.getById(id)

        assertEquals(25, result?.pokemon_id?.toInt())
        assertEquals("pikachu", result?.name)
        assertEquals("https://example.com/pikachu.png", result?.sprite_url)
        assertEquals(4, result?.height?.toInt())
        assertEquals(60, result?.weight?.toInt())
        assertEquals(123L, result?.fetched_at)
    }

    @Test
    fun getById_unknownId_returnsNull() = runTest {
        val result = dataSource.getById(999L)

        assertNull(result)
    }

    @Test
    fun insert_assignsIncrementingAutoIncrementIds() = runTest {
        val firstId = dataSource.insert(pokemonId = 1, name = "a", spriteUrl = "", height = 1, weight = 1, fetchedAt = 1L)
        val secondId = dataSource.insert(pokemonId = 1, name = "a", spriteUrl = "", height = 1, weight = 1, fetchedAt = 2L)

        assertEquals(firstId + 1, secondId)
    }

    // endregion

    // region observeAll

    @Test
    fun observeAll_whenEmpty_emitsEmptyList() = runTest {
        val result = dataSource.observeAll().first()

        assertEquals(emptyList(), result)
    }

    @Test
    fun observeAll_sameSpeciesInsertedTwice_returnsTwoSeparateRows() = runTest {
        dataSource.insert(pokemonId = 25, name = "pikachu", spriteUrl = "", height = 4, weight = 60, fetchedAt = 1L)
        dataSource.insert(pokemonId = 25, name = "pikachu", spriteUrl = "", height = 4, weight = 60, fetchedAt = 2L)

        val result = dataSource.observeAll().first()

        assertEquals(2, result.size)
    }

    @Test
    fun observeAll_ordersByFetchedAtDescending() = runTest {
        dataSource.insert(pokemonId = 1, name = "a", spriteUrl = "", height = 1, weight = 1, fetchedAt = 1L)
        dataSource.insert(pokemonId = 2, name = "b", spriteUrl = "", height = 1, weight = 1, fetchedAt = 3L)
        dataSource.insert(pokemonId = 3, name = "c", spriteUrl = "", height = 1, weight = 1, fetchedAt = 2L)

        val result = dataSource.observeAll().first()

        assertEquals(listOf(3L, 2L, 1L), result.map { it.fetched_at })
    }

    @Test
    fun observeAll_emitsAgainWhenRowInsertedAfterSubscription() = runTest {
        dataSource.observeAll().test {
            assertEquals(emptyList(), awaitItem())

            dataSource.insert(pokemonId = 25, name = "pikachu", spriteUrl = "", height = 4, weight = 60, fetchedAt = 1L)

            val afterInsert = awaitItem()
            assertEquals(1, afterInsert.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region deleteAll

    @Test
    fun deleteAll_removesAllRows() = runTest {
        dataSource.insert(pokemonId = 25, name = "pikachu", spriteUrl = "", height = 4, weight = 60, fetchedAt = 1L)

        dataSource.deleteAll()

        assertEquals(emptyList(), dataSource.observeAll().first())
    }

    // endregion
}
