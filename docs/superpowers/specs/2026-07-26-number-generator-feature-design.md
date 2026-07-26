# Number Generator Feature — Design Spec

## Goal

Build one feature, `number-generator`, inside the AppName KMP/CMP template project, following the architecture conventions already established by the reference codebases (PopKnow, Turnstile) and already partially scaffolded in this repo's `infra/`, `common/`, and `konsistTest/` modules. The feature is the deliverable: two screens (Generate, History) demonstrating the full data → domain → presentation stack, MVI conventions, Navigation 3 wiring, offline-aware sync, and Konsist-compliant structure.

This repo (`AppName`) is meant to be a standalone, reproducible architecture template — it must not depend on PopKnow or Turnstile at runtime; those two are read-only references for conventions only.

## Context & Key Discoveries

Research against the actual code (not just READMEs) surfaced three load-bearing facts that override the original literal request wording:

1. **MVI vocabulary.** The requested "State, Intent, Action, Command, Event" vocabulary was a hand-rolled MVI (`MviViewModel`/`Reducer`/`Next`/`Contract`) that once lived in Turnstile's `infra/mvi/`, but was deleted in commit `0795550 "Remove custom mvi"` and, per its own git history, was never actually adopted by any real screen. Every real screen in both PopKnow and Turnstile uses **MVIKotlin** (`StoreFactory`/`Executor`/`Reducer`) with vocabulary **Intent, Label, Action, Message, State** (still 5 declarations). AppName's own already-committed `infra/mvi/StoreModule.kt` wires `DefaultStoreFactory`, `mvikotlin` is already a declared Gradle dependency, and this repo's own `konsistTest/PresentationLayerTest.kt` already hard-codes the Intent/Label/Action/Message/State shape. **Decision (confirmed with user): use MVIKotlin's actual vocabulary**, not the deleted one.
2. **SQLDelight naming.** PopKnow's README claims "no Entity suffix," but PopKnow's actual `.sq` files (`QuestionHistoryEntity`, `CategoryEntity`) and AppName's own existing `Number.sq` (`NumberEntity`, a database-infra smoke-test table) both use the `Entity` suffix. **Decision (confirmed with user): use `GeneratedNumberEntity`**, matching real precedent over the README.
3. **Infra is genuinely off-limits.** `infra/network`, `infra/database`, `infra/mvi`, `infra/navigation`, `infra/platform` must not be modified. This has concrete consequences (see Data Layer and Connectivity below) — notably, no new SQLDelight column adapters (would require editing `infra/database/AppDatabase.kt`'s constructor call) and no shared `HttpClient` base-URL change (would require editing `infra/network/HttpClientFactory.kt`). `app/`, `common/`, and the new `feature/` package are fair game.

**Confirmed choices:**
- Random number source: `random.org` Integer Generator (`https://www.random.org/integers/?num=1&min=1&max=100&col=1&base=10&format=plain`), plain-text response.
- Fact source: `numbersapi.com/{n}?json`, JSON response.
- Number range: 1–100.

## Package Layout

`io.nicolaszurbuchen.appname.feature.numbergenerator` — single compound word, matching sibling features' naming style (`feature/quiz`, `feature/home`, `feature/stats` in PopKnow).

```
feature/numbergenerator/
├── data/
│   ├── datasource/
│   │   ├── remote/
│   │   │   ├── api/{RandomNumberApi,RandomNumberApiImpl,NumberFactApi,NumberFactApiImpl}.kt
│   │   │   ├── dto/NumberFactDto.kt
│   │   │   ├── mapper/NumberGeneratorRemoteMapper.kt
│   │   │   ├── RandomNumberRemoteDataSource(Impl).kt
│   │   │   └── NumberFactRemoteDataSource(Impl).kt
│   │   └── local/
│   │       ├── mapper/NumberGeneratorLocalMapper.kt
│   │       ├── GeneratedNumberLocalDataSource(Impl).kt
│   │       └── ConnectivityChecker.kt (expect) + ConnectivityChecker.android.kt / .ios.kt (actual)
│   └── repository/NumberGeneratorRepositoryImpl.kt
├── domain/
│   ├── model/GeneratedNumber.kt
│   ├── repository/NumberGeneratorRepository.kt
│   └── usecase/{GenerateNumberUseCase,ObserveHistoryUseCase,ToggleFavoriteUseCase,SyncPendingUseCase}.kt
├── di/NumberGeneratorModule.kt
└── presentation/
    ├── navigation/{NumberGeneratorDestination,NumberGeneratorNavigator,NumberGeneratorNavKeyHandler}.kt
    └── screen/
        ├── generate/{GenerateContract,GenerateStoreFactory,GenerateViewModel,GenerateRoute,GenerateScreen,GenerateUiModel,GenerateUiMapper,GenerateScreenPreview}.kt
        └── history/{HistoryContract,HistoryStoreFactory,HistoryViewModel,HistoryRoute,HistoryScreen,HistoryUiModel,HistoryUiMapper,HistoryScreenPreview}.kt
```

Plus the SQLDelight file: `shared/src/commonMain/sqldelight/io/nicolaszurbuchen/appname/feature/numbergenerator/data/datasource/local/GeneratedNumber.sq`.

## Domain Layer

- `GeneratedNumber(id: Long, value: Int, fact: String?, createdAt: Long, isFavorite: Boolean, isSynced: Boolean)` — pure data class.
- `NumberGeneratorRepository` — plain interface, no default bodies:
  - `suspend fun generateAndSave(): GeneratedNumber`
  - `fun observeHistory(): Flow<List<GeneratedNumber>>`
  - `suspend fun toggleFavorite(id: Long)`
  - `suspend fun syncPending()`
- Four use cases, each a plain class with a single `operator fun invoke(...)` forwarding to the repository. Per the established convention, touching a repository port is sufficient justification for a use case to exist (no need for extra logic beyond the call) — so these thin forwards are correct, not violations of the "no pure pass-through" rule. **The actual two-API composition lives in `NumberGeneratorRepositoryImpl`**, matching where PopKnow puts remote+local juggling (just extended to two remote sources instead of one).
  - `GenerateNumberUseCase(repository): GeneratedNumber`
  - `ObserveHistoryUseCase(repository): Flow<List<GeneratedNumber>>`
  - `ToggleFavoriteUseCase(repository, id: Long)`
  - `SyncPendingUseCase(repository)`

## Data Layer

**Remote:**
- `RandomNumberApi.getRandomNumber(min: Int, max: Int): Int` — calls random.org, reads the response via `bodyAsText()` (plain text, not JSON) and parses the trimmed string to `Int`. No Dto — the payload isn't structured, so introducing one would be a needless wrapper.
- `NumberFactApi.getFact(number: Int): NumberFactDto` — calls `numbersapi.com/{number}?json`, deserialized via the shared client's existing `ContentNegotiation` JSON config.
- `NumberFactDto(text: String, number: Int, found: Boolean, type: String)` — plain data class, no functions, no parents.
- Both Api impls reuse the **existing shared `HttpClient` singleton** (`infra/network/NetworkModule.kt`) but issue **absolute-URL requests** — an absolute URL in a Ktor request overrides the client's baked-in `defaultRequest` base URL, so no infra edit is needed to hit two different external hosts from one shared client.
- `RandomNumberRemoteDataSource(Impl)` / `NumberFactRemoteDataSource(Impl)` — each wraps its Api call in try/catch, translating failures into `AppException(AppError.NumberGenerator.NumberFetchFailed)` or `AppException(AppError.NumberGenerator.FactFetchFailed)` respectively (mirrors `QuizRemoteDataSourceImpl`'s translate-at-the-boundary pattern).
- `NumberGeneratorRemoteMapper.kt` — `fun NumberFactDto.toValue(): String? = text.takeIf { found }`. Named `toValue` (not e.g. `toFactText`) because the repo's `DataLayerTest` Konsist rule enforces mapper function names to match `to.*(Domain|Entity|Dto|Value|Enum)$`.

**Local:**
- `GeneratedNumberEntity` table:
  ```sql
  CREATE TABLE GeneratedNumberEntity (
      id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
      value INTEGER NOT NULL,
      fact TEXT,
      created_at INTEGER NOT NULL,
      is_favorite INTEGER NOT NULL DEFAULT 0,
      is_synced INTEGER NOT NULL DEFAULT 0
  );
  ```
  Queries: `insertGeneratedNumber`, `selectAllOrderByCreatedAtDesc`, `updateFavorite`, `updateFact`, `selectUnsynced`.
- `GeneratedNumberLocalDataSource(Impl)` wraps these queries; history is exposed reactively via `.asFlow().mapToList(Dispatchers.Default)` (same idiom as `HomeLocalDataSourceImpl`).
- `NumberGeneratorLocalMapper.kt` — `fun GeneratedNumberEntity.toDomain(): GeneratedNumber`, converting `INTEGER` 0/1 columns to `Boolean` manually in the mapper body. No custom SQLDelight column adapter (would require editing `infra/database/AppDatabase.kt`'s constructor call — off-limits).
- `ConnectivityChecker` — `expect class` with `fun isConnected(): Boolean`, actuals in `androidMain`/`iosMain` under the feature's own `data/datasource/local/` package (not `infra/platform/`, since that's off-limits). Android via `ConnectivityManager.activeNetwork`; iOS via a synchronous reachability snapshot.

**Repository:**
- `generateAndSave()`: fetches the random number first — a failure here is a **hard failure**: `AppException(NumberFetchFailed)` propagates, nothing is persisted. If that succeeds, checks `ConnectivityChecker.isConnected()`; only if online does it attempt the fact fetch. A fact-fetch failure (or being offline) is a **soft failure**: caught internally, row is persisted with `fact = null, isSynced = false`. If the fact fetch succeeds, both fields are persisted with `isSynced = true`. Returns the newly-saved `GeneratedNumber` either way (with a marker so the caller can tell soft-failure occurred — see Presentation layer).
- `syncPending()`: pulls unsynced rows, retries the fact fetch per row, updates on success, leaves untouched on continued failure (no exception propagates out of `syncPending()` itself).

## Error Handling

Added to `common/error/AppError.kt` (not infra):
```kotlin
sealed interface NumberGenerator : AppError {
    data object NumberFetchFailed : NumberGenerator
    data object FactFetchFailed : NumberGenerator
}
```

Both subtypes share the single `error: AppError?` slot in `GenerateState` (per the requested 3-field shape: `isLoading/lastGenerated/error`), but behave differently in the reducer:
- `NumberFetchFailed` — hard failure. `lastGenerated` is left unchanged, `error` is set, UI shows a blocking error state ("Couldn't reach the number generator").
- `FactFetchFailed` — soft failure. `generateAndSave()` doesn't throw for this case — the reducer sets `lastGenerated` to the newly-saved row (`fact = null`) **and** sets `error = FactFetchFailed` at the same time. UI renders this as a small non-blocking notice under the result ("Saved without a fun fact — will sync later") rather than replacing the screen. This is how the two subtypes get distinct UI copy without a 4th state field.

## Presentation Layer (MVIKotlin: Intent / Label / Action / Message / State)

**Screen 1 — Generate** (`presentation/screen/generate/`)
- `GenerateContract.kt`: `GenerateIntent { GenerateClicked }`; `GenerateLabel` (empty — no navigation triggered from this screen); `GenerateAction` (empty — generation is user-triggered only, no bootstrap fetch); `GenerateMessage { GenerationStarted, GenerationSucceeded(number), GenerationPartiallyFailed(number), GenerationFailed(error) }`; `GenerateState(isLoading: Boolean = false, lastGenerated: GeneratedNumber? = null, error: AppError? = null)`.
- `GenerateStoreFactory.kt`: executor calls `GenerateNumberUseCase()` on `GenerateClicked`, distinguishing `AppException(NumberFetchFailed)` (→ `GenerationFailed`) from a successfully-returned row with `fact == null && !isSynced` (→ `GenerationPartiallyFailed`) vs full success (→ `GenerationSucceeded`).
- `GenerateViewModel` / `GenerateRoute` / `GenerateScreen` — standard shape: button, loading spinner, result card, error banner.
- `GenerateUiModel` / `GenerateUiMapper` / `GenerateScreenPreview` — `UiModel` holds only primitives; `AppError` is resolved to display copy in the mapper.

**Screen 2 — History** (`presentation/screen/history/`)
- `HistoryContract.kt`: `HistoryIntent { ToggleFavorite(id: Long), SyncNowClicked }`; `HistoryLabel` (empty); `HistoryAction { ObserveHistory }` (dispatched by the bootstrapper); `HistoryMessage { HistoryUpdated(items), SyncStarted, SyncFinished(failureCount: Int) }`; `HistoryState(items: List<GeneratedNumber> = emptyList(), isSyncing: Boolean = false, syncError: AppError? = null)`.
- Executor collects `ObserveHistoryUseCase()`'s Flow inside `scope.launch`, dispatching `HistoryUpdated` per emission. `ToggleFavorite` / `SyncNowClicked` call their use cases directly — the list reflects changes automatically via the Flow re-emitting after each DB write.
- Same Route/Screen/UiModel/UiMapper/Preview set.

## Navigation (Navigation 3, mirrors PopKnow's Quiz feature)

- `NumberGeneratorDestination.kt`: `sealed interface NumberGeneratorDestination : NavKey`; `GenerateDestination`, `HistoryDestination` (both `@Serializable data object`).
- `NumberGeneratorNavigator.kt`: `interface { fun navigateToHistory(); fun navigateBack() }`.
- `NumberGeneratorNavKeyHandler.kt`: registers both `entry<>` blocks.
- `app/navigation/impl/NumberGeneratorNavigatorImpl.kt` (new file under `app/`, not infra): implements the interface via the existing `AppNavigator`.

## DI

`feature/numbergenerator/di/NumberGeneratorModule.kt` — `singleOf` for both Apis/DataSources/Repository, `factoryOf` for the 4 use cases + both StoreFactories, `viewModelOf` for both ViewModels. Same shape as PopKnow's `QuizModule`.

## Wiring into the App Shell

All of the following are `app/`-layer files (not infra) and will be edited:
- `app/di/AppModule.kt` — add `numberGeneratorModule` to the list.
- `app/navigation/NavigationModule.kt` — bind `NumberGeneratorNavigatorImpl`, register `NumberGeneratorNavKeyHandler`, change `initialRoute` from the current placeholder `InitialDestination` to `GenerateDestination` (there's no home/splash feature yet in this template, so Generate becomes the app's actual entry point).
- `app/navigation/NavConfig.kt` — register `GenerateDestination`/`HistoryDestination` in the polymorphic serializer module.
- `app/App.kt` — currently the unwired KMP-wizard "Click me!" placeholder. Replace with `AppNameTheme { NavGraph() }` (matching PopKnow's `App.kt`) so the feature is actually reachable.

## Konsist Compliance

Verified against the already-committed rules in `konsistTest/`:
- `DomainLayerTest`: use cases are plain classes with a single `invoke`, none inject another use case; repository is a plain interface with no default bodies. ✅
- `DataLayerTest`: Api/DataSource/RepositoryImpl naming and interface-vs-class shape; Dto is a plain data class with no functions/parents; mapper functions are top-level extension functions named `toValue`/`toDomain`. ✅
- `PresentationLayerTest`: each Contract has all 4 sealed interfaces (Intent/Label/Action/Message, some intentionally empty) plus a `State` data class; Route/Screen/ViewModel shape rules. ✅

No existing Konsist test file needs modification.

## Testing Scope (for the implementation plan)

- Unit tests for each screen's `ReducerImpl.reduce` (pure).
- `NumberGeneratorRepositoryImpl` under online / offline / fact-failure branches, using fakes for both remote data sources and the connectivity checker.
- The four use cases' forwarding behavior.

Per the `test-driven-development` skill, tests are written before implementation during the actual build — this spec only fixes scope, not test code.

## Out of Scope

- Any change to `infra/network`, `infra/database`, `infra/mvi`, `infra/navigation`, `infra/platform`.
- Fixing the currently-commented-out `AppErrorUiMapper.kt` (it's dead code today; this feature's screens resolve their own error copy locally instead of depending on it).
- Adding or editing Konsist test files (existing rules already cover the new code).
- A home/splash feature — Generate becomes the temporary initial route.
