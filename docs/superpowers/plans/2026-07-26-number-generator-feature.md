# Number Generator Feature Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the `number-generator` feature (Generate + History screens) inside `shared/src/.../feature/numbergenerator/`, fully wired into the AppName app shell, following this repo's Clean Architecture + MVIKotlin + Navigation 3 + Konsist conventions without modifying any file under `infra/`.

**Architecture:** Domain (pure model + repository interface + 4 thin use cases) → Data (two Ktor remote sources composed in the repository impl, one SQLDelight local source, an offline-aware repository impl) → Presentation (two MVIKotlin screens: Intent/Label/Action/Message/State contracts, StoreFactory/ViewModel/Route/Screen/UiModel/UiMapper/Preview) → Navigation 3 (feature Navigator interface + NavKeyHandler, `NumberGeneratorNavigatorImpl` in `app/navigation/impl`).

**Tech Stack:** Kotlin Multiplatform (Android + iOS), Compose Multiplatform, MVIKotlin (`StoreFactory`/`CoroutineExecutor`/`Reducer`), Koin, Ktor (two external hosts: random.org, numbersapi.com), SQLDelight + coroutines-extensions, Navigation 3, Konsist (existing rules only, none added).

Full design rationale: [docs/superpowers/specs/2026-07-26-number-generator-feature-design.md](../specs/2026-07-26-number-generator-feature-design.md).

**Package root for all new commonMain files:** `io.nicolaszurbuchen.appname.feature.numbergenerator` (directory: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/`).

**Hard constraint carried through every task:** never edit anything under `shared/src/*Main/kotlin/io/nicolaszurbuchen/appname/infra/`. Where the feature needs something infra normally provides (a Koin binding, a platform check), the binding is added in the feature's own `di/` module or `app/`-layer files instead.

---

### Task 1: Domain model, repository interface, AppError additions

**Files:**
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/domain/model/GeneratedNumber.kt`
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/domain/repository/NumberGeneratorRepository.kt`
- Modify: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/common/error/AppError.kt`

These are pure declarations (no behavior to TDD).

- [ ] **Step 1: Create the domain model**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.domain.model

data class GeneratedNumber(
    val id: Long,
    val value: Int,
    val fact: String?,
    val createdAt: Long,
    val isFavorite: Boolean,
    val isSynced: Boolean,
)
```

- [ ] **Step 2: Create the repository interface**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.domain.repository

import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.model.GeneratedNumber
import kotlinx.coroutines.flow.Flow

interface NumberGeneratorRepository {
    suspend fun generateAndSave(): GeneratedNumber

    fun observeHistory(): Flow<List<GeneratedNumber>>

    suspend fun toggleFavorite(id: Long)

    suspend fun syncPending()
}
```

- [ ] **Step 3: Add the two NumberGenerator AppError subtypes**

Open `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/common/error/AppError.kt` and add a new sealed interface alongside the existing `Trivia` one:

```kotlin
    sealed interface NumberGenerator : AppError {
        data object NumberFetchFailed : NumberGenerator

        data object FactFetchFailed : NumberGenerator
    }
```

Insert it directly after the closing brace of the existing `sealed interface Trivia { ... }` block and before `data class Unexpected(...)`, so the file reads:

```kotlin
package io.nicolaszurbuchen.appname.common.error

sealed interface AppError {
    sealed interface Network : AppError {
        data object Unavailable : Network

        data object Timeout : Network

        data class Http(
            val code: Int,
            val serverMessage: String? = null,
        ) : Network
    }

    sealed interface Database : AppError {
        data class QueryFailed(
            val cause: Throwable,
        ) : Database

        data class InsertFailed(
            val cause: Throwable,
        ) : Database
    }

    sealed interface Trivia : AppError {
        data object NoResults : Trivia

        data object InvalidParameter : Trivia

        data object RateLimit : Trivia
    }

    sealed interface NumberGenerator : AppError {
        data object NumberFetchFailed : NumberGenerator

        data object FactFetchFailed : NumberGenerator
    }

    data class Unexpected(
        val cause: Throwable,
    ) : AppError
}

fun Int.toAppError(): AppError =
    when (this) {
        1 -> AppError.Trivia.NoResults
        2 -> AppError.Trivia.InvalidParameter
        5 -> AppError.Trivia.RateLimit
        else -> AppError.Network.Http(this)
    }
```

- [ ] **Step 4: Build to confirm no compile errors**

Run: `.\gradlew.bat :shared:compileCommonMainKotlinMetadata`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/domain/model/GeneratedNumber.kt shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/domain/repository/NumberGeneratorRepository.kt shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/common/error/AppError.kt
git commit -m "feat(number-generator): add domain model, repository interface, and AppError subtypes"
```

---

### Task 2: Use cases (TDD with a fake repository)

**Files:**
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/domain/usecase/GenerateNumberUseCase.kt`
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/domain/usecase/ObserveHistoryUseCase.kt`
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/domain/usecase/ToggleFavoriteUseCase.kt`
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/domain/usecase/SyncPendingUseCase.kt`
- Test: `shared/src/commonTest/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/domain/usecase/UseCaseTest.kt`

- [ ] **Step 1: Write the failing tests, using a hand-written fake repository**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase

import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.model.GeneratedNumber
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.repository.NumberGeneratorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeNumberGeneratorRepository : NumberGeneratorRepository {
    var generateAndSaveResult: GeneratedNumber = GeneratedNumber(1, 42, "a fact", 1_000L, false, true)
    var historyFlow: Flow<List<GeneratedNumber>> = flowOf(emptyList())
    var toggleFavoriteCalledWith: Long? = null
    var syncPendingCalled = false

    override suspend fun generateAndSave(): GeneratedNumber = generateAndSaveResult

    override fun observeHistory(): Flow<List<GeneratedNumber>> = historyFlow

    override suspend fun toggleFavorite(id: Long) {
        toggleFavoriteCalledWith = id
    }

    override suspend fun syncPending() {
        syncPendingCalled = true
    }
}

class GenerateNumberUseCaseTest {
    @Test
    fun `invoke returns the repository result`() =
        runTest {
            val repository = FakeNumberGeneratorRepository()
            val useCase = GenerateNumberUseCase(repository)

            val result = useCase()

            assertEquals(repository.generateAndSaveResult, result)
        }
}

class ObserveHistoryUseCaseTest {
    @Test
    fun `invoke returns the repository history flow`() =
        runTest {
            val expected = listOf(GeneratedNumber(1, 7, null, 500L, true, false))
            val repository = FakeNumberGeneratorRepository().apply { historyFlow = flowOf(expected) }
            val useCase = ObserveHistoryUseCase(repository)

            val result = useCase().let { flow -> mutableListOf<List<GeneratedNumber>>().also { list -> flow.collect { list.add(it) } } }

            assertEquals(listOf(expected), result)
        }
}

class ToggleFavoriteUseCaseTest {
    @Test
    fun `invoke forwards the id to the repository`() =
        runTest {
            val repository = FakeNumberGeneratorRepository()
            val useCase = ToggleFavoriteUseCase(repository)

            useCase(42L)

            assertEquals(42L, repository.toggleFavoriteCalledWith)
        }
}

class SyncPendingUseCaseTest {
    @Test
    fun `invoke calls syncPending on the repository`() =
        runTest {
            val repository = FakeNumberGeneratorRepository()
            val useCase = SyncPendingUseCase(repository)

            useCase()

            assertTrue(repository.syncPendingCalled)
        }
}
```

- [ ] **Step 2: Add `kotlinx-coroutines-test` to commonTest dependencies**

`runTest` requires `kotlinx-coroutines-test`, which isn't declared yet. Open `gradle/libs.versions.toml` and add, next to the existing `coroutines` entries:

```toml
coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
```

(Use whatever the existing `coroutines` version ref is named in this file — check the `[versions]` block for the key backing `coroutines-core`/`coroutines-android` and reuse that same ref.)

Then in `shared/build.gradle.kts`, add it to the `commonTest.dependencies` block (currently only `implementation(libs.kotlin.test)`):

```kotlin
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
        }
```

- [ ] **Step 3: Run the tests to verify they fail**

Run: `.\gradlew.bat :shared:testAndroidHostTest --tests "io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase.*"`
Expected: FAIL to compile — `GenerateNumberUseCase`, `ObserveHistoryUseCase`, `ToggleFavoriteUseCase`, `SyncPendingUseCase` are unresolved references.

- [ ] **Step 4: Implement the four use cases**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase

import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.model.GeneratedNumber
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.repository.NumberGeneratorRepository

class GenerateNumberUseCase(
    private val repository: NumberGeneratorRepository,
) {
    suspend operator fun invoke(): GeneratedNumber = repository.generateAndSave()
}
```

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase

import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.model.GeneratedNumber
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.repository.NumberGeneratorRepository
import kotlinx.coroutines.flow.Flow

class ObserveHistoryUseCase(
    private val repository: NumberGeneratorRepository,
) {
    operator fun invoke(): Flow<List<GeneratedNumber>> = repository.observeHistory()
}
```

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase

import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.repository.NumberGeneratorRepository

class ToggleFavoriteUseCase(
    private val repository: NumberGeneratorRepository,
) {
    suspend operator fun invoke(id: Long) = repository.toggleFavorite(id)
}
```

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase

import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.repository.NumberGeneratorRepository

class SyncPendingUseCase(
    private val repository: NumberGeneratorRepository,
) {
    suspend operator fun invoke() = repository.syncPending()
}
```

- [ ] **Step 5: Run the tests to verify they pass**

Run: `.\gradlew.bat :shared:testAndroidHostTest --tests "io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase.*"`
Expected: BUILD SUCCESSFUL, 4 tests passed.

- [ ] **Step 6: Commit**

```bash
git add gradle/libs.versions.toml shared/build.gradle.kts shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/domain/usecase/ shared/src/commonTest/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/domain/usecase/
git commit -m "feat(number-generator): add use cases with fake-repository tests"
```

---

### Task 3: SQLDelight schema + local mapper (TDD)

**Files:**
- Create: `shared/src/commonMain/sqldelight/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/local/GeneratedNumber.sq`
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/local/mapper/NumberGeneratorLocalMapper.kt`
- Test: `shared/src/commonTest/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/local/mapper/NumberGeneratorLocalMapperTest.kt`

SQLDelight generates `GeneratedNumberEntity` (the row type) and `GeneratedNumberQueries` (the queries type, named after the `.sq` file) into the package matching the `.sq` file's own directory — `io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local` — NOT the `io.nicolaszurbuchen.appname.cache` package (that package, set via `sqldelight { databases { create("AppDatabase") { packageName.set(...) } } }` in `shared/build.gradle.kts`, governs only the top-level `AppDatabase` class itself). Column names are kept verbatim as Kotlin property names (snake_case in, snake_case out), **except** the `value` column, which SQLDelight escapes to the Kotlin property `value_` because `value` collides with a Kotlin soft keyword — confirmed against the actual generated `GeneratedNumberEntity.kt` under `shared/build/generated/sqldelight/...` after running the generation task in Task 3.

- [ ] **Step 1: Create the SQLDelight schema**

```sql
CREATE TABLE GeneratedNumberEntity (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    value INTEGER NOT NULL,
    fact TEXT,
    created_at INTEGER NOT NULL,
    is_favorite INTEGER NOT NULL DEFAULT 0,
    is_synced INTEGER NOT NULL DEFAULT 0
);

insertGeneratedNumber:
INSERT INTO GeneratedNumberEntity(value, fact, created_at, is_favorite, is_synced)
VALUES (?, ?, ?, ?, ?);

lastInsertRowId:
SELECT last_insert_rowid();

selectAllOrderByCreatedAtDesc:
SELECT * FROM GeneratedNumberEntity
ORDER BY created_at DESC;

selectById:
SELECT * FROM GeneratedNumberEntity WHERE id = ?;

updateFavorite:
UPDATE GeneratedNumberEntity SET is_favorite = ? WHERE id = ?;

updateFact:
UPDATE GeneratedNumberEntity SET fact = ?, is_synced = ? WHERE id = ?;

selectUnsynced:
SELECT * FROM GeneratedNumberEntity WHERE is_synced = 0;
```

- [ ] **Step 2: Generate SQLDelight sources so `GeneratedNumberEntity` exists for the test/mapper to reference**

Run: `.\gradlew.bat :shared:generateCommonMainAppDatabaseInterface`
Expected: BUILD SUCCESSFUL (generates `GeneratedNumberEntity` and `GeneratedNumberQueries` under `build/generated/sqldelight/...`).

- [ ] **Step 3: Write the failing mapper test**

```kotlin
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
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\gradlew.bat :shared:testAndroidHostTest --tests "io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local.mapper.NumberGeneratorLocalMapperTest"`
Expected: FAIL to compile — `toDomain` is unresolved.

- [ ] **Step 5: Implement the mapper**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local.mapper

import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local.GeneratedNumberEntity
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.model.GeneratedNumber

fun GeneratedNumberEntity.toDomain(): GeneratedNumber =
    GeneratedNumber(
        id = id,
        value = value_.toInt(),
        fact = fact,
        createdAt = created_at,
        isFavorite = is_favorite == 1L,
        isSynced = is_synced == 1L,
    )
```

(Note: SQLDelight generates `GeneratedNumberEntity`/`GeneratedNumberQueries` in the package matching the `.sq` file's own directory — `io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local` — not in `io.nicolaszurbuchen.appname.cache`, which only holds the top-level `AppDatabase` class. The `value` column is also escaped to the Kotlin property `value_`, since `value` is a Kotlin soft keyword. Both corrections verified against the actual generated code.)

- [ ] **Step 6: Run test to verify it passes**

Run: `.\gradlew.bat :shared:testAndroidHostTest --tests "io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local.mapper.NumberGeneratorLocalMapperTest"`
Expected: BUILD SUCCESSFUL, 2 tests passed.

- [ ] **Step 7: Commit**

```bash
git add shared/src/commonMain/sqldelight/io/nicolaszurbuchen/appname/feature/numbergenerator/ shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/local/mapper/ shared/src/commonTest/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/local/mapper/
git commit -m "feat(number-generator): add GeneratedNumberEntity schema and local mapper"
```

---

### Task 4: Local data source

**Files:**
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/local/GeneratedNumberLocalDataSource.kt`
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/local/GeneratedNumberLocalDataSourceImpl.kt`

Thin wrapper around generated queries — matches this repo's existing `QuizLocalDataSourceImpl`/`HomeLocalDataSourceImpl`, which have no dedicated unit tests (I/O wrappers, not logic). No TDD step here, consistent with that precedent.

- [ ] **Step 1: Create the interface**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local

import kotlinx.coroutines.flow.Flow

interface GeneratedNumberLocalDataSource {
    suspend fun insert(
        value: Int,
        fact: String?,
        createdAt: Long,
        isSynced: Boolean,
    ): Long

    fun observeAll(): Flow<List<GeneratedNumberEntity>>

    suspend fun getById(id: Long): GeneratedNumberEntity?

    suspend fun setFavorite(
        id: Long,
        isFavorite: Boolean,
    )

    suspend fun updateFact(
        id: Long,
        fact: String,
        isSynced: Boolean,
    )

    suspend fun getUnsynced(): List<GeneratedNumberEntity>
}
```

- [ ] **Step 2: Create the implementation**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class GeneratedNumberLocalDataSourceImpl(
    private val queries: GeneratedNumberQueries,
) : GeneratedNumberLocalDataSource {
    override suspend fun insert(
        value: Int,
        fact: String?,
        createdAt: Long,
        isSynced: Boolean,
    ): Long =
        queries.transactionWithResult {
            queries.insertGeneratedNumber(
                value_ = value.toLong(),
                fact = fact,
                created_at = createdAt,
                is_favorite = 0L,
                is_synced = if (isSynced) 1L else 0L,
            )
            queries.lastInsertRowId().executeAsOne()
        }

    override fun observeAll(): Flow<List<GeneratedNumberEntity>> =
        queries.selectAllOrderByCreatedAtDesc().asFlow().mapToList(Dispatchers.Default)

    override suspend fun getById(id: Long): GeneratedNumberEntity? = queries.selectById(id).executeAsOneOrNull()

    override suspend fun setFavorite(
        id: Long,
        isFavorite: Boolean,
    ) {
        queries.updateFavorite(is_favorite = if (isFavorite) 1L else 0L, id = id)
    }

    override suspend fun updateFact(
        id: Long,
        fact: String,
        isSynced: Boolean,
    ) {
        queries.updateFact(fact = fact, is_synced = if (isSynced) 1L else 0L, id = id)
    }

    override suspend fun getUnsynced(): List<GeneratedNumberEntity> = queries.selectUnsynced().executeAsList()
}
```

- [ ] **Step 3: Build to confirm it compiles**

Run: `.\gradlew.bat :shared:compileCommonMainKotlinMetadata`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/local/GeneratedNumberLocalDataSource.kt shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/local/GeneratedNumberLocalDataSourceImpl.kt
git commit -m "feat(number-generator): add local data source"
```

---

### Task 5: Connectivity checker (interface + platform module, no infra edits)

**Files:**
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/platform/ConnectivityChecker.kt`
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/di/NumberGeneratorPlatformModule.kt`
- Create: `shared/src/androidMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/di/NumberGeneratorPlatformModule.kt`
- Create: `shared/src/iosMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/di/NumberGeneratorPlatformModule.kt`
- Modify: `androidApp/src/main/AndroidManifest.xml`

`ConnectivityChecker` is a **plain interface** (fakeable in tests), not an `expect class` — this is what makes `NumberGeneratorRepositoryImpl` testable with a fake in Task 6. The real platform implementations are bound via an `expect val Module` (a Koin module value, not a class), which is a legitimate KMP pattern and keeps the Context-dependent Android wiring out of commonMain without touching `infra/di/PlatformModule.kt`.

- [ ] **Step 1: Create the common interface**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.data.platform

interface ConnectivityChecker {
    fun isConnected(): Boolean
}
```

- [ ] **Step 2: Declare the expect platform module**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.di

import org.koin.core.module.Module

expect val numberGeneratorPlatformModule: Module
```

- [ ] **Step 3: Add the Android permissions needed to make any network/connectivity check work**

This repo's `AndroidManifest.xml` currently declares no permissions at all — not even `INTERNET`, which every Ktor call needs. Open `androidApp/src/main/AndroidManifest.xml` and add both permissions before the `<application>` tag:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:name=".AppNameApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">
        <activity
            android:exported="true"
            android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

- [ ] **Step 4: Implement the Android actual module**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.platform.ConnectivityChecker
import org.koin.core.module.Module
import org.koin.dsl.module

private class AndroidConnectivityChecker(
    private val context: Context,
) : ConnectivityChecker {
    override fun isConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

actual val numberGeneratorPlatformModule: Module =
    module {
        single<ConnectivityChecker> { AndroidConnectivityChecker(get()) }
    }
```

This relies on Koin's Android `Context` already being registered via `androidContext(this@AppNameApplication)` in `androidApp/src/main/kotlin/io/nicolaszurbuchen/appname/AppNameApplication.kt` (already present, not being changed) — `get()` here resolves that existing binding.

- [ ] **Step 5: Implement the iOS actual module**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.di

import io.nicolaszurbuchen.appname.feature.numbergenerator.data.platform.ConnectivityChecker
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.SystemConfiguration.SCNetworkReachabilityCreateWithName
import platform.SystemConfiguration.SCNetworkReachabilityFlagsVar
import platform.SystemConfiguration.SCNetworkReachabilityGetFlags
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsReachable

@OptIn(ExperimentalForeignApi::class)
private class IosConnectivityChecker : ConnectivityChecker {
    override fun isConnected(): Boolean =
        memScoped {
            val reachability = SCNetworkReachabilityCreateWithName(null, "www.random.org") ?: return false
            val flags = alloc<SCNetworkReachabilityFlagsVar>()
            val success = SCNetworkReachabilityGetFlags(reachability, flags.ptr)
            success && (flags.value and kSCNetworkReachabilityFlagsReachable) != 0u
        }
}

actual val numberGeneratorPlatformModule: Module =
    module {
        single<ConnectivityChecker> { IosConnectivityChecker() }
    }
```

Note: `SystemConfiguration` reachability symbol names have shifted slightly across Kotlin/Native versions. If the iOS build (Step 6) reports an unresolved reference here, check the exact import path reported by the compiler error and adjust — the flag-check logic itself (`GetFlags` + bitwise AND against `kSCNetworkReachabilityFlagsReachable`) stays the same.

- [ ] **Step 6: Build both platform targets**

Run: `.\gradlew.bat :shared:compileAndroidMain`
Expected: BUILD SUCCESSFUL

Run: `.\gradlew.bat :shared:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL (adjust cinterop imports per the note above if this fails)

- [ ] **Step 7: Commit**

```bash
git add shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/platform/ConnectivityChecker.kt shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/di/NumberGeneratorPlatformModule.kt shared/src/androidMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/di/NumberGeneratorPlatformModule.kt shared/src/iosMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/di/NumberGeneratorPlatformModule.kt androidApp/src/main/AndroidManifest.xml
git commit -m "feat(number-generator): add connectivity checker with platform-specific Koin module"
```

---

### Task 6: Remote DTO + remote mapper (TDD)

**Files:**
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/remote/dto/NumberFactDto.kt`
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/remote/mapper/NumberGeneratorRemoteMapper.kt`
- Test: `shared/src/commonTest/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/remote/mapper/NumberGeneratorRemoteMapperTest.kt`

- [ ] **Step 1: Create the Dto**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NumberFactDto(
    val text: String,
    val number: Int,
    val found: Boolean,
    val type: String,
)
```

- [ ] **Step 2: Write the failing mapper test**

```kotlin
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
```

- [ ] **Step 3: Run test to verify it fails**

Run: `.\gradlew.bat :shared:testAndroidHostTest --tests "io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.mapper.NumberGeneratorRemoteMapperTest"`
Expected: FAIL to compile — `toValue` unresolved.

- [ ] **Step 4: Implement the mapper**

Named `toValue` (not e.g. `toFactText`) because this repo's `konsistTest/DataLayerTest.kt` enforces mapper function names to match `to.*(Domain|Entity|Dto|Value|Enum)$`.

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.mapper

import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.dto.NumberFactDto

fun NumberFactDto.toValue(): String? = text.takeIf { found }
```

- [ ] **Step 5: Run test to verify it passes**

Run: `.\gradlew.bat :shared:testAndroidHostTest --tests "io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.mapper.NumberGeneratorRemoteMapperTest"`
Expected: BUILD SUCCESSFUL, 2 tests passed.

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/remote/dto/ shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/remote/mapper/ shared/src/commonTest/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/remote/mapper/
git commit -m "feat(number-generator): add NumberFactDto and remote mapper"
```

---

### Task 7: Ktor Api services

**Files:**
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/remote/api/RandomNumberApi.kt`
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/remote/api/RandomNumberApiImpl.kt`
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/remote/api/NumberFactApi.kt`
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/remote/api/NumberFactApiImpl.kt`

Both reuse the existing shared `HttpClient` (bound in `infra/network/NetworkModule.kt`, unmodified) but issue **absolute-URL** requests, which override that client's baked-in placeholder `defaultRequest` base URL. No infra edit needed. Thin IO wrappers — no dedicated unit test, matching this repo's `QuizApiImpl` precedent.

- [ ] **Step 1: Random number Api — random.org returns plain text, not JSON**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.api

interface RandomNumberApi {
    suspend fun getRandomNumber(
        min: Int,
        max: Int,
    ): Int
}
```

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText

class RandomNumberApiImpl(
    private val client: HttpClient,
) : RandomNumberApi {
    override suspend fun getRandomNumber(
        min: Int,
        max: Int,
    ): Int {
        val response =
            client.get("https://www.random.org/integers/") {
                parameter("num", 1)
                parameter("min", min)
                parameter("max", max)
                parameter("col", 1)
                parameter("base", 10)
                parameter("format", "plain")
            }.bodyAsText()
        return response.trim().toInt()
    }
}
```

- [ ] **Step 2: Number fact Api — numbersapi.com returns JSON**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.api

import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.dto.NumberFactDto

interface NumberFactApi {
    suspend fun getFact(number: Int): NumberFactDto
}
```

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.dto.NumberFactDto

class NumberFactApiImpl(
    private val client: HttpClient,
) : NumberFactApi {
    override suspend fun getFact(number: Int): NumberFactDto =
        client.get("https://numbersapi.com/$number") {
            parameter("json", true)
        }.body()
}
```

- [ ] **Step 3: Build to confirm it compiles**

Run: `.\gradlew.bat :shared:compileCommonMainKotlinMetadata`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/remote/api/
git commit -m "feat(number-generator): add RandomNumberApi and NumberFactApi"
```

---

### Task 8: Remote data sources (error translation)

**Files:**
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/remote/RandomNumberRemoteDataSource.kt`
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/remote/RandomNumberRemoteDataSourceImpl.kt`
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/remote/NumberFactRemoteDataSource.kt`
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/remote/NumberFactRemoteDataSourceImpl.kt`

These are what `NumberGeneratorRepositoryImpl` (Task 9) will depend on and fake in its own test — no dedicated test here (thin translation wrappers, matching `QuizRemoteDataSourceImpl` precedent).

- [ ] **Step 1: Random number remote data source**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote

interface RandomNumberRemoteDataSource {
    suspend fun fetchRandomNumber(
        min: Int,
        max: Int,
    ): Int
}
```

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote

import io.nicolaszurbuchen.appname.common.error.AppError
import io.nicolaszurbuchen.appname.common.error.AppException
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.api.RandomNumberApi
import kotlinx.coroutines.CancellationException

class RandomNumberRemoteDataSourceImpl(
    private val api: RandomNumberApi,
) : RandomNumberRemoteDataSource {
    override suspend fun fetchRandomNumber(
        min: Int,
        max: Int,
    ): Int =
        try {
            api.getRandomNumber(min, max)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            throw AppException(AppError.NumberGenerator.NumberFetchFailed)
        }
}
```

(Note: `catch (e: CancellationException) { throw e }` must come before the generic `catch (_: Exception)` — otherwise a coroutine cancellation would be swallowed and converted into a business error instead of propagating, breaking structured concurrency.)

- [ ] **Step 2: Number fact remote data source**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote

interface NumberFactRemoteDataSource {
    suspend fun fetchFact(number: Int): String?
}
```

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote

import io.nicolaszurbuchen.appname.common.error.AppError
import io.nicolaszurbuchen.appname.common.error.AppException
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.api.NumberFactApi
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.mapper.toValue
import kotlinx.coroutines.CancellationException

class NumberFactRemoteDataSourceImpl(
    private val api: NumberFactApi,
) : NumberFactRemoteDataSource {
    override suspend fun fetchFact(number: Int): String? =
        try {
            api.getFact(number).toValue()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            throw AppException(AppError.NumberGenerator.FactFetchFailed)
        }
}
```

- [ ] **Step 3: Build to confirm it compiles**

Run: `.\gradlew.bat :shared:compileCommonMainKotlinMetadata`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/remote/RandomNumberRemoteDataSource.kt shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/remote/RandomNumberRemoteDataSourceImpl.kt shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/remote/NumberFactRemoteDataSource.kt shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/remote/NumberFactRemoteDataSourceImpl.kt
git commit -m "feat(number-generator): add remote data sources with AppException translation"
```

---

### Task 9: Repository implementation (TDD — the core orchestration logic)

**Files:**
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/repository/NumberGeneratorRepositoryImpl.kt`
- Test: `shared/src/commonTest/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/repository/NumberGeneratorRepositoryImplTest.kt`

This is the most important test in the feature: it proves the online/offline/fact-failure branching described in the spec.

- [ ] **Step 1: Write the failing tests, using fakes for both remote data sources, the local data source, and the connectivity checker**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.data.repository

import io.nicolaszurbuchen.appname.common.error.AppError
import io.nicolaszurbuchen.appname.common.error.AppException
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.platform.ConnectivityChecker
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local.GeneratedNumberEntity
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local.GeneratedNumberLocalDataSource
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.NumberFactRemoteDataSource
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.RandomNumberRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeRandomNumberRemoteDataSource(
    private val result: Int? = null,
    private val failure: Exception? = null,
) : RandomNumberRemoteDataSource {
    override suspend fun fetchRandomNumber(
        min: Int,
        max: Int,
    ): Int = failure?.let { throw it } ?: result!!
}

private class FakeNumberFactRemoteDataSource(
    private val result: String? = null,
    private val failure: Exception? = null,
) : NumberFactRemoteDataSource {
    override suspend fun fetchFact(number: Int): String? = failure?.let { throw it } ?: result
}

private class FakeConnectivityChecker(
    private val connected: Boolean,
) : ConnectivityChecker {
    override fun isConnected(): Boolean = connected
}

private class FakeGeneratedNumberLocalDataSource : GeneratedNumberLocalDataSource {
    var lastInsert: Triple<Int, String?, Boolean>? = null
    var rows: MutableList<GeneratedNumberEntity> = mutableListOf()
    var lastSetFavorite: Pair<Long, Boolean>? = null

    override suspend fun insert(
        value: Int,
        fact: String?,
        createdAt: Long,
        isSynced: Boolean,
    ): Long {
        lastInsert = Triple(value, fact, isSynced)
        return 1L
    }

    override fun observeAll(): Flow<List<GeneratedNumberEntity>> = flowOf(emptyList())

    override suspend fun getById(id: Long): GeneratedNumberEntity? = rows.find { it.id == id }

    override suspend fun setFavorite(
        id: Long,
        isFavorite: Boolean,
    ) {
        lastSetFavorite = id to isFavorite
    }

    override suspend fun updateFact(
        id: Long,
        fact: String,
        isSynced: Boolean,
    ) = Unit

    override suspend fun getUnsynced(): List<GeneratedNumberEntity> = emptyList()
}

class NumberGeneratorRepositoryImplTest {
    @Test
    fun `generateAndSave persists both fields when online and the fact call succeeds`() =
        runTest {
            val local = FakeGeneratedNumberLocalDataSource()
            val repository =
                NumberGeneratorRepositoryImpl(
                    randomNumberRemoteDataSource = FakeRandomNumberRemoteDataSource(result = 42),
                    numberFactRemoteDataSource = FakeNumberFactRemoteDataSource(result = "42 is nice"),
                    localDataSource = local,
                    connectivityChecker = FakeConnectivityChecker(connected = true),
                )

            val result = repository.generateAndSave()

            assertEquals(42, result.value)
            assertEquals("42 is nice", result.fact)
            assertTrue(result.isSynced)
            assertEquals(Triple(42, "42 is nice", true), local.lastInsert)
        }

    @Test
    fun `generateAndSave persists with a null fact when offline`() =
        runTest {
            val local = FakeGeneratedNumberLocalDataSource()
            val repository =
                NumberGeneratorRepositoryImpl(
                    randomNumberRemoteDataSource = FakeRandomNumberRemoteDataSource(result = 7),
                    numberFactRemoteDataSource = FakeNumberFactRemoteDataSource(result = "unused"),
                    localDataSource = local,
                    connectivityChecker = FakeConnectivityChecker(connected = false),
                )

            val result = repository.generateAndSave()

            assertEquals(7, result.value)
            assertEquals(null, result.fact)
            assertFalse(result.isSynced)
            assertEquals(Triple(7, null, false), local.lastInsert)
        }

    @Test
    fun `generateAndSave persists with a null fact when the fact call fails`() =
        runTest {
            val local = FakeGeneratedNumberLocalDataSource()
            val repository =
                NumberGeneratorRepositoryImpl(
                    randomNumberRemoteDataSource = FakeRandomNumberRemoteDataSource(result = 13),
                    numberFactRemoteDataSource =
                        FakeNumberFactRemoteDataSource(failure = AppException(AppError.NumberGenerator.FactFetchFailed)),
                    localDataSource = local,
                    connectivityChecker = FakeConnectivityChecker(connected = true),
                )

            val result = repository.generateAndSave()

            assertEquals(13, result.value)
            assertEquals(null, result.fact)
            assertFalse(result.isSynced)
        }

    @Test
    fun `generateAndSave throws and persists nothing when the number call fails`() =
        runTest {
            val local = FakeGeneratedNumberLocalDataSource()
            val repository =
                NumberGeneratorRepositoryImpl(
                    randomNumberRemoteDataSource =
                        FakeRandomNumberRemoteDataSource(failure = AppException(AppError.NumberGenerator.NumberFetchFailed)),
                    numberFactRemoteDataSource = FakeNumberFactRemoteDataSource(result = "unused"),
                    localDataSource = local,
                    connectivityChecker = FakeConnectivityChecker(connected = true),
                )

            val exception = assertFailsWith<AppException> { repository.generateAndSave() }

            assertEquals(AppError.NumberGenerator.NumberFetchFailed, exception.error)
            assertEquals(null, local.lastInsert)
        }

    @Test
    fun `toggleFavorite flips the current favorite state of the row`() =
        runTest {
            val local =
                FakeGeneratedNumberLocalDataSource().apply {
                    rows.add(GeneratedNumberEntity(5L, 9L, "fact", 100L, 0L, 1L))
                }
            val repository =
                NumberGeneratorRepositoryImpl(
                    randomNumberRemoteDataSource = FakeRandomNumberRemoteDataSource(result = 1),
                    numberFactRemoteDataSource = FakeNumberFactRemoteDataSource(result = "unused"),
                    localDataSource = local,
                    connectivityChecker = FakeConnectivityChecker(connected = true),
                )

            repository.toggleFavorite(5L)

            assertEquals(5L to true, local.lastSetFavorite)
        }

    @Test
    fun `toggleFavorite does nothing when the row no longer exists`() =
        runTest {
            val local = FakeGeneratedNumberLocalDataSource()
            val repository =
                NumberGeneratorRepositoryImpl(
                    randomNumberRemoteDataSource = FakeRandomNumberRemoteDataSource(result = 1),
                    numberFactRemoteDataSource = FakeNumberFactRemoteDataSource(result = "unused"),
                    localDataSource = local,
                    connectivityChecker = FakeConnectivityChecker(connected = true),
                )

            repository.toggleFavorite(999L)

            assertEquals(null, local.lastSetFavorite)
        }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `.\gradlew.bat :shared:testAndroidHostTest --tests "io.nicolaszurbuchen.appname.feature.numbergenerator.data.repository.NumberGeneratorRepositoryImplTest"`
Expected: FAIL to compile — `NumberGeneratorRepositoryImpl` unresolved.

- [ ] **Step 3: Implement the repository**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.data.repository

import io.nicolaszurbuchen.appname.feature.numbergenerator.data.platform.ConnectivityChecker
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local.GeneratedNumberLocalDataSource
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local.mapper.toDomain
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.NumberFactRemoteDataSource
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.RandomNumberRemoteDataSource
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.model.GeneratedNumber
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.repository.NumberGeneratorRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

private const val MIN_VALUE = 1
private const val MAX_VALUE = 100

class NumberGeneratorRepositoryImpl(
    private val randomNumberRemoteDataSource: RandomNumberRemoteDataSource,
    private val numberFactRemoteDataSource: NumberFactRemoteDataSource,
    private val localDataSource: GeneratedNumberLocalDataSource,
    private val connectivityChecker: ConnectivityChecker,
) : NumberGeneratorRepository {
    override suspend fun generateAndSave(): GeneratedNumber {
        val value = randomNumberRemoteDataSource.fetchRandomNumber(MIN_VALUE, MAX_VALUE)

        val fact =
            if (connectivityChecker.isConnected()) {
                try {
                    numberFactRemoteDataSource.fetchFact(value)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    null
                }
            } else {
                null
            }

        val createdAt = Clock.System.now().toEpochMilliseconds()
        val isSynced = fact != null
        val id = localDataSource.insert(value = value, fact = fact, createdAt = createdAt, isSynced = isSynced)

        return GeneratedNumber(
            id = id,
            value = value,
            fact = fact,
            createdAt = createdAt,
            isFavorite = false,
            isSynced = isSynced,
        )
    }

    override fun observeHistory(): Flow<List<GeneratedNumber>> = localDataSource.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun toggleFavorite(id: Long) {
        // observeHistory() reflects the change automatically once the row is updated.
        val current = localDataSource.getById(id) ?: return
        localDataSource.setFavorite(id, current.is_favorite != 1L)
    }

    override suspend fun syncPending() {
        localDataSource.getUnsynced().forEach { row ->
            try {
                val fact = numberFactRemoteDataSource.fetchFact(row.value_.toInt())
                if (fact != null) {
                    localDataSource.updateFact(row.id, fact, isSynced = true)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Leave this row unsynced; the next manual sync will retry it.
            }
        }
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `.\gradlew.bat :shared:testAndroidHostTest --tests "io.nicolaszurbuchen.appname.feature.numbergenerator.data.repository.NumberGeneratorRepositoryImplTest"`
Expected: BUILD SUCCESSFUL, 6 tests passed.

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/sqldelight/io/nicolaszurbuchen/appname/feature/numbergenerator/ shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/ shared/src/commonTest/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/data/
git commit -m "feat(number-generator): add repository implementation with offline-aware generateAndSave"
```

---

### Task 10: Feature DI module (data + domain)

**Files:**
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/di/NumberGeneratorModule.kt`

This is the file that binds `GeneratedNumberQueries` off the existing `AppDatabase` singleton — done here, in the feature's own module, specifically so `infra/database/DatabaseModule.kt` never needs to change.

- [ ] **Step 1: Create the module with data + domain bindings**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.di

import io.nicolaszurbuchen.appname.cache.AppDatabase
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local.GeneratedNumberLocalDataSource
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local.GeneratedNumberLocalDataSourceImpl
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.NumberFactRemoteDataSource
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.NumberFactRemoteDataSourceImpl
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.RandomNumberRemoteDataSource
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.RandomNumberRemoteDataSourceImpl
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.api.NumberFactApi
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.api.NumberFactApiImpl
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.api.RandomNumberApi
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.api.RandomNumberApiImpl
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.repository.NumberGeneratorRepositoryImpl
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.repository.NumberGeneratorRepository
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase.GenerateNumberUseCase
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase.ObserveHistoryUseCase
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase.SyncPendingUseCase
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase.ToggleFavoriteUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val numberGeneratorModule =
    module {
        single { get<AppDatabase>().generatedNumberQueries }

        singleOf(::RandomNumberApiImpl) bind RandomNumberApi::class
        singleOf(::NumberFactApiImpl) bind NumberFactApi::class

        singleOf(::RandomNumberRemoteDataSourceImpl) bind RandomNumberRemoteDataSource::class
        singleOf(::NumberFactRemoteDataSourceImpl) bind NumberFactRemoteDataSource::class
        singleOf(::GeneratedNumberLocalDataSourceImpl) bind GeneratedNumberLocalDataSource::class

        singleOf(::NumberGeneratorRepositoryImpl) bind NumberGeneratorRepository::class

        factoryOf(::GenerateNumberUseCase)
        factoryOf(::ObserveHistoryUseCase)
        factoryOf(::ToggleFavoriteUseCase)
        factoryOf(::SyncPendingUseCase)
    }
```

(Presentation bindings — StoreFactories and ViewModels — are added to this same file in Tasks 11 and 12.)

- [ ] **Step 2: Build to confirm it compiles**

Run: `.\gradlew.bat :shared:compileCommonMainKotlinMetadata`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/di/NumberGeneratorModule.kt
git commit -m "feat(number-generator): add data/domain DI bindings"
```

---

### Task 11: Generate screen

**Files:**
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/presentation/screen/generate/GenerateContract.kt`
- Create: `.../generate/GenerateReducer.kt`
- Create: `.../generate/GenerateStoreFactory.kt`
- Create: `.../generate/GenerateViewModel.kt`
- Create: `.../generate/GenerateUiModel.kt`
- Create: `.../generate/GenerateUiMapper.kt`
- Create: `.../generate/GenerateRoute.kt`
- Create: `.../generate/GenerateScreen.kt`
- Create: `.../generate/GenerateScreenPreview.kt`
- Test: `shared/src/commonTest/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/presentation/screen/generate/GenerateReducerTest.kt`
- Modify: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/di/NumberGeneratorModule.kt`

**Deliberate deviation from `QuizStoreFactory`'s style, worth calling out:** PopKnow nests its reducer as a `private object ReducerImpl` inside the StoreFactory class, which makes it untestable from outside. Since this spec calls for reducer unit tests, `GenerateReducer` (and `HistoryReducer` in Task 12) are extracted to their own top-level `internal object` files instead — same pure-function contract, just reachable from `commonTest`.

- [ ] **Step 1: Contract**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate

import io.nicolaszurbuchen.appname.common.error.AppError
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.model.GeneratedNumber

sealed interface GenerateIntent {
    data object GenerateClicked : GenerateIntent
}

sealed interface GenerateLabel

sealed interface GenerateAction

sealed interface GenerateMessage {
    data object GenerationStarted : GenerateMessage

    data class GenerationSucceeded(
        val number: GeneratedNumber,
    ) : GenerateMessage

    data class GenerationPartiallyFailed(
        val number: GeneratedNumber,
    ) : GenerateMessage

    data class GenerationFailed(
        val error: AppError,
    ) : GenerateMessage
}

data class GenerateState(
    val isLoading: Boolean = false,
    val lastGenerated: GeneratedNumber? = null,
    val error: AppError? = null,
)
```

- [ ] **Step 2: Write the failing reducer test**

```kotlin
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
```

- [ ] **Step 3: Run test to verify it fails**

Run: `.\gradlew.bat :shared:testAndroidHostTest --tests "io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate.GenerateReducerTest"`
Expected: FAIL to compile — `GenerateReducer` unresolved.

- [ ] **Step 4: Implement the reducer**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate

import com.arkivanov.mvikotlin.core.store.Reducer
import io.nicolaszurbuchen.appname.common.error.AppError

internal object GenerateReducer : Reducer<GenerateState, GenerateMessage> {
    override fun GenerateState.reduce(msg: GenerateMessage): GenerateState =
        when (msg) {
            GenerateMessage.GenerationStarted -> copy(isLoading = true, error = null)

            is GenerateMessage.GenerationSucceeded -> copy(isLoading = false, lastGenerated = msg.number, error = null)

            is GenerateMessage.GenerationPartiallyFailed ->
                copy(
                    isLoading = false,
                    lastGenerated = msg.number,
                    error = AppError.NumberGenerator.FactFetchFailed,
                )

            is GenerateMessage.GenerationFailed -> copy(isLoading = false, error = msg.error)
        }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `.\gradlew.bat :shared:testAndroidHostTest --tests "io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate.GenerateReducerTest"`
Expected: BUILD SUCCESSFUL, 4 tests passed.

- [ ] **Step 6: StoreFactory**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import io.nicolaszurbuchen.appname.common.error.AppError
import io.nicolaszurbuchen.appname.common.error.AppException
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase.GenerateNumberUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

interface GenerateStore : Store<GenerateIntent, GenerateState, GenerateLabel>

class GenerateStoreFactory(
    private val storeFactory: StoreFactory,
    private val generateNumber: GenerateNumberUseCase,
) {
    fun create(): GenerateStore =
        object :
            GenerateStore,
            Store<GenerateIntent, GenerateState, GenerateLabel> by storeFactory.create(
                name = "GenerateStore",
                initialState = GenerateState(),
                executorFactory = { ExecutorImpl() },
                reducer = GenerateReducer,
            ) {}

    private inner class ExecutorImpl : CoroutineExecutor<GenerateIntent, GenerateAction, GenerateState, GenerateMessage, GenerateLabel>() {
        override fun executeIntent(intent: GenerateIntent) {
            when (intent) {
                GenerateIntent.GenerateClicked -> performGenerate()
            }
        }

        private fun performGenerate() {
            dispatch(GenerateMessage.GenerationStarted)
            scope.launch {
                try {
                    val number = generateNumber()
                    if (number.fact == null && !number.isSynced) {
                        dispatch(GenerateMessage.GenerationPartiallyFailed(number))
                    } else {
                        dispatch(GenerateMessage.GenerationSucceeded(number))
                    }
                } catch (e: AppException) {
                    dispatch(GenerateMessage.GenerationFailed(e.error))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    dispatch(GenerateMessage.GenerationFailed(AppError.Unexpected(e)))
                }
            }
        }
    }
}
```

- [ ] **Step 7: ViewModel**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class GenerateViewModel(
    factory: GenerateStoreFactory,
) : ViewModel() {
    private val store = factory.create()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<GenerateUiModel> =
        store.stateFlow
            .map { it.toUiModel() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GenerateState().toUiModel())

    val labels: Flow<GenerateLabel> = store.labels

    fun onIntent(intent: GenerateIntent) {
        store.accept(intent)
    }

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
```

- [ ] **Step 8: UiModel + UiMapper**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate

data class GenerateUiModel(
    val isLoading: Boolean,
    val resultValue: String?,
    val resultFact: String?,
    val errorMessage: String?,
    val isPartialFailureNotice: Boolean,
)
```

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate

import io.nicolaszurbuchen.appname.common.error.AppError

fun GenerateState.toUiModel(): GenerateUiModel =
    GenerateUiModel(
        isLoading = isLoading,
        resultValue = lastGenerated?.value?.toString(),
        resultFact = lastGenerated?.fact,
        errorMessage = error?.toMessage(lastGenerated != null),
        isPartialFailureNotice = error == AppError.NumberGenerator.FactFetchFailed,
    )

private fun AppError.toMessage(hasResult: Boolean): String? =
    when (this) {
        AppError.NumberGenerator.NumberFetchFailed -> "Couldn't reach the number generator. Try again."
        AppError.NumberGenerator.FactFetchFailed ->
            if (hasResult) "Saved without a fun fact — will sync later." else null
        else -> "Something went wrong. Try again."
    }
```

- [ ] **Step 9: Route + Screen**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GenerateRoute(
    onNavigateToHistory: () -> Unit,
    viewModel: GenerateViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    GenerateScreen(
        state = state,
        onGenerateClick = { viewModel.onIntent(GenerateIntent.GenerateClicked) },
        onHistoryClick = onNavigateToHistory,
    )
}
```

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GenerateScreen(
    state: GenerateUiModel,
    onGenerateClick: () -> Unit,
    onHistoryClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.isLoading) {
            CircularProgressIndicator()
        }

        state.resultValue?.let { value ->
            Text(text = value, style = MaterialTheme.typography.displayMedium)
            state.resultFact?.let { fact -> Text(text = fact, style = MaterialTheme.typography.bodyLarge) }
        }

        state.errorMessage?.let { message ->
            Text(
                text = message,
                color =
                    if (state.isPartialFailureNotice) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
            )
        }

        Button(onClick = onGenerateClick, enabled = !state.isLoading) {
            Text("Generate a number")
        }

        TextButton(onClick = onHistoryClick) {
            Text("View history")
        }
    }
}
```

- [ ] **Step 10: Preview**

```kotlin
@file:Suppress("ktlint:standard:filename")

package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.nicolaszurbuchen.appname.app.design.theme.AppNameTheme

class GenerateUiModelProvider : PreviewParameterProvider<GenerateUiModel> {
    override val values =
        sequenceOf(
            GenerateUiModel(isLoading = true, resultValue = null, resultFact = null, errorMessage = null, isPartialFailureNotice = false),
            GenerateUiModel(
                isLoading = false,
                resultValue = "42",
                resultFact = "42 is the number of laws of cricket.",
                errorMessage = null,
                isPartialFailureNotice = false,
            ),
            GenerateUiModel(
                isLoading = false,
                resultValue = "7",
                resultFact = null,
                errorMessage = "Saved without a fun fact — will sync later.",
                isPartialFailureNotice = true,
            ),
            GenerateUiModel(
                isLoading = false,
                resultValue = null,
                resultFact = null,
                errorMessage = "Couldn't reach the number generator. Try again.",
                isPartialFailureNotice = false,
            ),
        )
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
private fun GenerateScreenPreview(
    @PreviewParameter(GenerateUiModelProvider::class) state: GenerateUiModel,
) {
    AppNameTheme {
        GenerateScreen(state = state, onGenerateClick = {}, onHistoryClick = {})
    }
}
```

- [ ] **Step 11: Add StoreFactory + ViewModel bindings to the DI module**

Edit `NumberGeneratorModule.kt`, adding these imports and lines:

```kotlin
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate.GenerateStoreFactory
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate.GenerateViewModel
import org.koin.core.module.dsl.viewModelOf
```

and inside the `module { }` block, after the use case lines:

```kotlin
        factoryOf(::GenerateStoreFactory)
        viewModelOf(::GenerateViewModel)
```

- [ ] **Step 12: Build to confirm everything compiles**

Run: `.\gradlew.bat :shared:compileCommonMainKotlinMetadata`
Expected: BUILD SUCCESSFUL

- [ ] **Step 13: Commit**

```bash
git add shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/presentation/screen/generate/ shared/src/commonTest/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/presentation/screen/generate/ shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/di/NumberGeneratorModule.kt
git commit -m "feat(number-generator): add Generate screen with reducer tests"
```

---

### Task 12: History screen

**Files:**
- Create: `.../history/HistoryContract.kt`
- Create: `.../history/HistoryReducer.kt`
- Create: `.../history/HistoryStoreFactory.kt`
- Create: `.../history/HistoryViewModel.kt`
- Create: `.../history/HistoryUiModel.kt`
- Create: `.../history/HistoryUiMapper.kt`
- Create: `.../history/HistoryRoute.kt`
- Create: `.../history/HistoryScreen.kt`
- Create: `.../history/HistoryScreenPreview.kt`
- Test: `shared/src/commonTest/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/presentation/screen/history/HistoryReducerTest.kt`
- Modify: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/di/NumberGeneratorModule.kt`

- [ ] **Step 1: Contract**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history

import io.nicolaszurbuchen.appname.common.error.AppError
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.model.GeneratedNumber

sealed interface HistoryIntent {
    data class ToggleFavorite(
        val id: Long,
    ) : HistoryIntent

    data object SyncNowClicked : HistoryIntent
}

sealed interface HistoryLabel

sealed interface HistoryAction {
    data object ObserveHistory : HistoryAction
}

sealed interface HistoryMessage {
    data class HistoryUpdated(
        val items: List<GeneratedNumber>,
    ) : HistoryMessage

    data object SyncStarted : HistoryMessage

    data class SyncFinished(
        val error: AppError?,
    ) : HistoryMessage
}

data class HistoryState(
    val items: List<GeneratedNumber> = emptyList(),
    val isSyncing: Boolean = false,
    val syncError: AppError? = null,
)
```

- [ ] **Step 2: Write the failing reducer test**

```kotlin
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
```

- [ ] **Step 3: Run test to verify it fails**

Run: `.\gradlew.bat :shared:testAndroidHostTest --tests "io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history.HistoryReducerTest"`
Expected: FAIL to compile — `HistoryReducer` unresolved.

- [ ] **Step 4: Implement the reducer**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history

import com.arkivanov.mvikotlin.core.store.Reducer

internal object HistoryReducer : Reducer<HistoryState, HistoryMessage> {
    override fun HistoryState.reduce(msg: HistoryMessage): HistoryState =
        when (msg) {
            is HistoryMessage.HistoryUpdated -> copy(items = msg.items)
            HistoryMessage.SyncStarted -> copy(isSyncing = true, syncError = null)
            is HistoryMessage.SyncFinished -> copy(isSyncing = false, syncError = msg.error)
        }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `.\gradlew.bat :shared:testAndroidHostTest --tests "io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history.HistoryReducerTest"`
Expected: BUILD SUCCESSFUL, 3 tests passed.

- [ ] **Step 6: StoreFactory**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import io.nicolaszurbuchen.appname.common.error.AppError
import io.nicolaszurbuchen.appname.common.error.AppException
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase.ObserveHistoryUseCase
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase.SyncPendingUseCase
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

interface HistoryStore : Store<HistoryIntent, HistoryState, HistoryLabel>

class HistoryStoreFactory(
    private val storeFactory: StoreFactory,
    private val observeHistory: ObserveHistoryUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val syncPending: SyncPendingUseCase,
) {
    fun create(): HistoryStore =
        object :
            HistoryStore,
            Store<HistoryIntent, HistoryState, HistoryLabel> by storeFactory.create(
                name = "HistoryStore",
                initialState = HistoryState(),
                bootstrapper = BootstrapperImpl(),
                executorFactory = { ExecutorImpl() },
                reducer = HistoryReducer,
            ) {}

    private class BootstrapperImpl : CoroutineBootstrapper<HistoryAction>() {
        override fun invoke() {
            dispatch(HistoryAction.ObserveHistory)
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<HistoryIntent, HistoryAction, HistoryState, HistoryMessage, HistoryLabel>() {
        override fun executeAction(action: HistoryAction) {
            when (action) {
                HistoryAction.ObserveHistory -> {
                    scope.launch {
                        observeHistory().collect { items ->
                            dispatch(HistoryMessage.HistoryUpdated(items))
                        }
                    }
                }
            }
        }

        override fun executeIntent(intent: HistoryIntent) {
            when (intent) {
                is HistoryIntent.ToggleFavorite -> {
                    scope.launch { toggleFavorite(intent.id) }
                }

                HistoryIntent.SyncNowClicked -> {
                    performSync()
                }
            }
        }

        private fun performSync() {
            dispatch(HistoryMessage.SyncStarted)
            scope.launch {
                try {
                    syncPending()
                    dispatch(HistoryMessage.SyncFinished(error = null))
                } catch (e: AppException) {
                    dispatch(HistoryMessage.SyncFinished(error = e.error))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    dispatch(HistoryMessage.SyncFinished(error = AppError.Unexpected(e)))
                }
            }
        }
    }
}
```

- [ ] **Step 7: ViewModel**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(
    factory: HistoryStoreFactory,
) : ViewModel() {
    private val store = factory.create()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<HistoryUiModel> =
        store.stateFlow
            .map { it.toUiModel() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryState().toUiModel())

    val labels: Flow<HistoryLabel> = store.labels

    fun onIntent(intent: HistoryIntent) {
        store.accept(intent)
    }

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
```

- [ ] **Step 8: UiModel + UiMapper**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history

data class HistoryUiModel(
    val items: List<HistoryItemUiModel>,
    val isSyncing: Boolean,
    val syncErrorMessage: String?,
)

data class HistoryItemUiModel(
    val id: Long,
    val valueText: String,
    val factText: String?,
    val isFavorite: Boolean,
    val isSynced: Boolean,
)
```

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history

fun HistoryState.toUiModel(): HistoryUiModel =
    HistoryUiModel(
        items =
            items.map { number ->
                HistoryItemUiModel(
                    id = number.id,
                    valueText = number.value.toString(),
                    factText = number.fact,
                    isFavorite = number.isFavorite,
                    isSynced = number.isSynced,
                )
            },
        isSyncing = isSyncing,
        syncErrorMessage = syncError?.let { "Sync failed. Try again." },
    )
```

- [ ] **Step 9: Route + Screen**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryRoute(
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HistoryScreen(
        state = state,
        onToggleFavorite = { viewModel.onIntent(HistoryIntent.ToggleFavorite(it)) },
        onSyncNowClick = { viewModel.onIntent(HistoryIntent.SyncNowClicked) },
        onBackClick = onNavigateBack,
    )
}
```

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HistoryScreen(
    state: HistoryUiModel,
    onToggleFavorite: (Long) -> Unit,
    onSyncNowClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = onBackClick) { Text("Back") }
            Button(onClick = onSyncNowClick, enabled = !state.isSyncing) {
                Text(if (state.isSyncing) "Syncing…" else "Sync now")
            }
        }

        state.syncErrorMessage?.let { message ->
            Text(text = message, color = MaterialTheme.colorScheme.error)
        }

        LazyColumn {
            items(state.items, key = { it.id }) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(text = item.valueText, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = item.factText ?: if (item.isSynced) "" else "Fact pending sync",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    IconButton(onClick = { onToggleFavorite(item.id) }) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Toggle favorite",
                        )
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 10: Preview**

```kotlin
@file:Suppress("ktlint:standard:filename")

package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.nicolaszurbuchen.appname.app.design.theme.AppNameTheme

class HistoryUiModelProvider : PreviewParameterProvider<HistoryUiModel> {
    override val values =
        sequenceOf(
            HistoryUiModel(
                items =
                    listOf(
                        HistoryItemUiModel(1, "42", "42 is the number of laws of cricket.", true, true),
                        HistoryItemUiModel(2, "7", null, false, false),
                    ),
                isSyncing = false,
                syncErrorMessage = null,
            ),
            HistoryUiModel(items = emptyList(), isSyncing = true, syncErrorMessage = null),
            HistoryUiModel(items = emptyList(), isSyncing = false, syncErrorMessage = "Sync failed. Try again."),
        )
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
private fun HistoryScreenPreview(
    @PreviewParameter(HistoryUiModelProvider::class) state: HistoryUiModel,
) {
    AppNameTheme {
        HistoryScreen(state = state, onToggleFavorite = {}, onSyncNowClick = {}, onBackClick = {})
    }
}
```

- [ ] **Step 11: Add StoreFactory + ViewModel bindings to the DI module**

Edit `NumberGeneratorModule.kt`, adding:

```kotlin
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history.HistoryStoreFactory
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history.HistoryViewModel
```

and, in the module block:

```kotlin
        factoryOf(::HistoryStoreFactory)
        viewModelOf(::HistoryViewModel)
```

- [ ] **Step 12: Build to confirm everything compiles**

Run: `.\gradlew.bat :shared:compileCommonMainKotlinMetadata`
Expected: BUILD SUCCESSFUL

- [ ] **Step 13: Commit**

```bash
git add shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/presentation/screen/history/ shared/src/commonTest/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/presentation/screen/history/ shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/di/NumberGeneratorModule.kt
git commit -m "feat(number-generator): add History screen with reducer tests"
```

---

### Task 13: Navigation

**Files:**
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/presentation/navigation/NumberGeneratorDestination.kt`
- Create: `.../navigation/NumberGeneratorNavigator.kt`
- Create: `.../navigation/NumberGeneratorNavKeyHandler.kt`

- [ ] **Step 1: Destinations**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface NumberGeneratorDestination : NavKey

@Serializable
data object GenerateDestination : NumberGeneratorDestination

@Serializable
data object HistoryDestination : NumberGeneratorDestination
```

- [ ] **Step 2: Navigator interface**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.navigation

interface NumberGeneratorNavigator {
    fun navigateToHistory()

    fun navigateBack()
}
```

- [ ] **Step 3: NavKeyHandler**

```kotlin
package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate.GenerateRoute
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history.HistoryRoute
import io.nicolaszurbuchen.appname.infra.navigation.NavKeyHandler

class NumberGeneratorNavKeyHandler(
    private val navigator: NumberGeneratorNavigator,
) : NavKeyHandler {
    override fun EntryProviderScope<NavKey>.registerEntries() {
        entry<GenerateDestination> {
            GenerateRoute(onNavigateToHistory = { navigator.navigateToHistory() })
        }

        entry<HistoryDestination> {
            HistoryRoute(onNavigateBack = { navigator.navigateBack() })
        }
    }
}
```

- [ ] **Step 4: Build to confirm it compiles**

Run: `.\gradlew.bat :shared:compileCommonMainKotlinMetadata`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/feature/numbergenerator/presentation/navigation/
git commit -m "feat(number-generator): add navigation destinations, navigator interface, and NavKeyHandler"
```

---

### Task 14: Wire the feature into the app shell

**Files:**
- Create: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/app/navigation/impl/NumberGeneratorNavigatorImpl.kt`
- Modify: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/app/di/AppModule.kt`
- Modify: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/app/navigation/NavigationModule.kt`
- Modify: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/app/navigation/NavConfig.kt`
- Modify: `shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/app/App.kt`

None of these are under `infra/` — they're the app-composition layer, which is exactly where cross-feature wiring is supposed to live per the established convention.

- [ ] **Step 1: NavigatorImpl**

```kotlin
package io.nicolaszurbuchen.appname.app.navigation.impl

import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.navigation.HistoryDestination
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.navigation.NumberGeneratorNavigator
import io.nicolaszurbuchen.appname.infra.navigation.AppNavigator

class NumberGeneratorNavigatorImpl(
    private val appNavigator: AppNavigator,
) : NumberGeneratorNavigator {
    override fun navigateToHistory() {
        appNavigator.navigateTo(HistoryDestination)
    }

    override fun navigateBack() {
        appNavigator.navigateBack()
    }
}
```

- [ ] **Step 2: Update `AppModule.kt` to include the feature module and its platform module**

```kotlin
package io.nicolaszurbuchen.appname.app.di

import io.nicolaszurbuchen.appname.app.navigation.appNavigationModule
import io.nicolaszurbuchen.appname.feature.numbergenerator.di.numberGeneratorModule
import io.nicolaszurbuchen.appname.feature.numbergenerator.di.numberGeneratorPlatformModule
import io.nicolaszurbuchen.appname.infra.database.databaseModule
import io.nicolaszurbuchen.appname.infra.mvi.storeModule
import io.nicolaszurbuchen.appname.infra.navigation.infraNavigationModule
import io.nicolaszurbuchen.appname.infra.network.networkModule

val appModule = listOf(
    appNavigationModule,
    databaseModule,
    infraNavigationModule,
    networkModule,
    storeModule,
    numberGeneratorModule,
    numberGeneratorPlatformModule,
)
```

- [ ] **Step 3: Update `app/navigation/NavigationModule.kt`**

```kotlin
package io.nicolaszurbuchen.appname.app.navigation

import androidx.navigation3.runtime.NavKey
import io.nicolaszurbuchen.appname.app.navigation.impl.NumberGeneratorNavigatorImpl
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.navigation.GenerateDestination
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.navigation.NumberGeneratorNavKeyHandler
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.navigation.NumberGeneratorNavigator
import io.nicolaszurbuchen.appname.infra.navigation.NavKeyHandler
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val appNavigationModule = module {
    single<NavKey>(named("initialRoute")) { GenerateDestination }

    singleOf(::NumberGeneratorNavigatorImpl) bind NumberGeneratorNavigator::class

    singleOf(::NumberGeneratorNavKeyHandler) { named("numberGenerator") } bind NavKeyHandler::class
}
```

- [ ] **Step 4: Update `app/navigation/NavConfig.kt`**

```kotlin
package io.nicolaszurbuchen.appname.app.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.navigation.GenerateDestination
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.navigation.HistoryDestination
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val navConfig =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(GenerateDestination::class)
                    subclass(HistoryDestination::class)
                }
            }
    }
```

Note: this removes the now-unused `InitialDestination` declaration that previously lived in this file. Delete that `@Serializable data object InitialDestination : NavKey` block entirely — it's replaced by `GenerateDestination` as the initial route.

- [ ] **Step 5: Update `App.kt`**

```kotlin
package io.nicolaszurbuchen.appname.app

import androidx.compose.runtime.Composable
import io.nicolaszurbuchen.appname.app.design.theme.AppNameTheme
import io.nicolaszurbuchen.appname.infra.navigation.NavGraph

@Composable
fun App() {
    AppNameTheme {
        NavGraph()
    }
}
```

- [ ] **Step 6: Build both platform targets**

Run: `.\gradlew.bat :shared:compileAndroidMain`
Expected: BUILD SUCCESSFUL

Run: `.\gradlew.bat :shared:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add shared/src/commonMain/kotlin/io/nicolaszurbuchen/appname/app/
git commit -m "feat(number-generator): wire the feature into the app shell as the initial route"
```

---

### Task 15: Full verification

**Files:** none (verification only)

- [ ] **Step 1: Run the full shared test suite**

Run: `.\gradlew.bat :shared:testAndroidHostTest`
Expected: BUILD SUCCESSFUL — all tests across every task pass together (use case, mapper, repository, and both reducer test classes).

- [ ] **Step 2: Run the Konsist architecture tests**

Run: `.\gradlew.bat :konsistTest:test`
Expected: BUILD SUCCESSFUL — confirms the new feature satisfies `DomainLayerTest`, `DataLayerTest`, and `PresentationLayerTest` without any changes to those test files.

- [ ] **Step 3: Assemble the Android debug APK**

Run: `.\gradlew.bat :androidApp:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Build the iOS framework**

Run: `.\gradlew.bat :shared:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Manually verify the app in the Android emulator/device**

Launch the app (`androidApp`). Expected: the Generate screen appears as the first screen (no more "Click me!" placeholder). Tapping "Generate a number" shows a loading indicator, then a number 1–100 with a fact underneath (or a "Saved without a fun fact" notice if offline/fact API is unreachable). "View history" navigates to the History screen showing the generated row; the star icon toggles favorite; "Sync now" retries any unsynced rows.

- [ ] **Step 6: Commit any final fixes found during manual verification**

If Step 5 surfaces bugs, fix them, re-run Steps 1–4, then:

```bash
git add -A
git commit -m "fix(number-generator): address issues found during manual verification"
```

(Skip this step entirely if Step 5 finds nothing to fix.)
