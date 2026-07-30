# AppName

**AppName** is a Kotlin Multiplatform / Compose Multiplatform application template targeting Android and iOS. It ships with one complete, fully-tested example feature — **pokemon-explorer** (fetch a random Pokémon, browse fetch history, view detail) — built against the public [PokéAPI](https://pokeapi.co/), which exists purely to demonstrate every architectural convention end to end: data/domain/presentation layering, MVI, DI wiring, navigation, error handling, and a full unit + architecture test suite.

The primary goal of this repository is **not the example app** — it is a living template. Every decision is deliberate and codified, with a strict separation between MVI state, domain models, and render targets enforced at compile-test time via Konsist. See [CLAUDE.md](CLAUDE.md) for the full checklist of what to change when you fork this into your own project.

---

## 🧱 Tech Stack

### 🧩 Architecture
- Clean Architecture (Data, Domain, Presentation layers)
- MVI with MVIKotlin (StoreFactory, Executor, Reducer pattern)
- UseCase-driven domain interaction, reserved for logic that touches a port (a repository, a clock) — a UseCase that would just forward to a single pure domain call doesn't exist
- Each repository juggles a remote (Ktor) and local (SQLDelight) data source, with neither leaking past the repository boundary
- Konsist for structural architecture enforcement, organized by rule category rather than by layer, including rules that enforce **test coverage itself** (see [`TestingTest.kt`](konsistTest/src/test/kotlin/io/nicolaszurbuchen/appname/TestingTest.kt))

### 🛠 Libraries
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) — shared UI for Android and iOS
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [MVIKotlin](https://arkivanov.github.io/MVIKotlin/) — MVI framework
- [Koin](https://insert-koin.io/) — dependency injection
- [Navigation 3](https://developer.android.com/guide/navigation/navigation-3) — KMP-compatible artifacts, type-safe `NavKey` destinations
- [Ktor](https://ktor.io/) — remote data sources
- [SQLDelight](https://cashapp.github.io/sqldelight/) — local data sources, per-row history persistence
- [Coil3](https://coil-kt.github.io/coil/) — image loading, wired to the shared Ktor `HttpClient`
- [Compose Multiplatform resources](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-resources.html) — shared strings across platforms
- [Konsist](https://docs.konsist.lemonappdev.com/) — architecture test enforcement
- [Turbine](https://github.com/cashapp/turbine) — Flow/Label testing
- [ktlint](https://pinterest.github.io/ktlint/) + [ktlint-compose-rules](https://mrmans0n.github.io/compose-rules/)
- [Husky](https://typicode.github.io/husky/) + [Commitlint](https://commitlint.js.org/) — conventional commit enforcement via Git hooks

---

## 📁 Project Structure

```
shared/
├── app/
│   ├── App.kt                       # Root Composable: theme + image loader + NavGraph
│   ├── design/
│   │   ├── component/               # App-wide reusable composables (e.g. AppErrorBanner)
│   │   └── theme/                   # Design tokens, color palette, spacing, typography
│   ├── di/
│   │   ├── KoinInitializer.kt
│   │   └── AppModule.kt             # Aggregates every feature/infra Koin module
│   └── navigation/
│       ├── impl/
│       │   └── PokemonExplorerNavigatorImpl.kt
│       ├── NavConfig.kt
│       └── NavigationModule.kt
├── common/                          # Cross-cutting concepts shared across features
│   └── error/                       # AppError / AppException, single throw-catch mechanism
├── feature/
│   └── pokemonexplorer/
│       ├── data/
│       │   ├── datasource/
│       │   │   ├── local/
│       │   │   │   ├── mapper/PokemonLocalMapper.kt
│       │   │   │   ├── PokemonLocalDataSource.kt
│       │   │   │   └── PokemonLocalDataSourceImpl.kt
│       │   │   └── remote/
│       │   │       ├── api/{PokemonApi.kt, PokemonApiImpl.kt}
│       │   │       ├── dto/{PokemonDto.kt, PokemonSpritesDto.kt}
│       │   │       ├── mapper/PokemonRemoteMapper.kt
│       │   │       └── PokemonRemoteDataSource(Impl).kt
│       │   └── repository/PokemonExplorerRepositoryImpl.kt
│       ├── di/PokemonExplorerModule.kt
│       ├── domain/
│       │   ├── model/Pokemon.kt
│       │   ├── repository/PokemonExplorerRepository.kt
│       │   └── usecase/{GetRandomPokemonUseCase, ObserveHistoryUseCase, ClearHistoryUseCase, GetPokemonByIdUseCase}.kt
│       └── presentation/
│           ├── navigation/{PokemonExplorerDestination, PokemonExplorerNavigator, PokemonExplorerNavKeyHandler}.kt
│           └── screen/
│               ├── main/
│               │   ├── MainContract.kt        # Intent/Label/Action/Message/State
│               │   ├── MainRoute.kt / MainScreen.kt
│               │   ├── MainStoreFactory.kt    # Bootstrapper + Executor + nested internal ReducerImpl
│               │   ├── MainUiMapper.kt / MainUiModel.kt / MainViewModel.kt
│               │   └── component/             # Screen-local composables (PokemonListItem, shimmer)
│               └── detail/                    # same shape as main/, no component/ subfolder needed
└── infra/
    ├── database/                     # SQLDelight driver setup (expect/actual)
    ├── mvi/                          # MVIKotlin base wiring (StoreFactory binding)
    ├── navigation/                   # AppNavigator, NavKeyHandler, NavGraph — feature-agnostic
    ├── network/                      # Ktor client configuration (expect/actual engine)
    ├── platform/                     # expect/actual platform utilities (BackHandler, Platform)
    └── ui/                           # UiText — resource/raw/composite text abstraction
androidApp/                           # Android application module (MainActivity, manifest)
konsistTest/                          # Separate module, sibling of shared/, architecture + coverage enforcement tests
```

---

## 🏛 Architecture Decisions

### Package structure
Four top-level packages, each with a distinct responsibility — this is the first thing to get right, since Konsist enforces it at compile-test time:
- `app/` — app-specific composition: `App.kt`, the design system (`design/component`, `design/theme`), root DI aggregation (`AppModule.kt`), and the concrete `*NavigatorImpl` classes that wire features together. **This is the only place allowed to know about more than one feature at once.**
- `infra/` — reusable technical plumbing with **zero domain or feature knowledge**: database driver setup, MVI base wiring, the root nav graph (`AppNavigator`, `NavKeyHandler`), network client configuration, platform abstractions. If a file here ever needs to import something from `feature.*`, it belongs in `app/` instead.
- `common/` — cross-cutting domain concepts genuinely shared across features (currently just `AppError`/`AppException`). A single-feature template has little here; it grows as a second feature needs to share a domain concept with the first. Don't put something in `common/` just because it *might* be reused — wait until a second feature actually needs it.
- `feature/` — vertical feature slices, each owning its full `data/domain/presentation` stack. A feature never imports another feature's internals (Konsist's `LayerBoundariesTest` enforces this).

**Decision rule of thumb:** ask "does this know about a specific feature?" — if yes, it's `app/` (if it knows about *multiple* features) or `feature/<name>/` (if it knows about just one). If it knows about *zero* features, it's `infra/` (pure technical plumbing) or `common/` (shared domain vocabulary, not technical plumbing).

### Domain layer conventions
- Domain models are pure values — no `Flow`/`StateFlow`, no internal mutability.
- `*UseCase` is reserved for logic that touches a port (a repository, a clock, anything with a managed lifecycle) or coordinates more than one step. A `UseCase` that only forwards to a single repository call still exists here (see `GetRandomPokemonUseCase`) because the repository *is* the port — but a `UseCase` must never inject another `UseCase` (Konsist enforces this).
- Non-deterministic values (current time, random numbers) needed by a class under test should be injected as a **defaulted constructor parameter** for determinism in tests — e.g. `currentTimeMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() }`. See the Koin gotcha below before doing this with anything wired through `singleOf`.
- Magic numbers get a named constant *and* a comment explaining where the bound comes from (e.g. `MIN_POKEMON_ID`/`MAX_POKEMON_ID` with a comment citing PokéAPI's actual valid id range) — a bare unexplained constant invites exactly the "why 1025?" question it should have pre-empted.
- SQLDelight primary keys: use an autoincrement row id, not a natural/business key, whenever the same real-world entity can legitimately appear more than once (e.g. fetch history) — a natural key silently overwrites instead of accumulating.

### Presentation layer conventions
Screen folders contain exactly:
- `*Contract.kt` — exactly `*Intent`, `*Label`, `*Action`, `*Message` (sealed interfaces) and `*State` (the only top-level `data class` allowed in this file). Nothing else lives here.
- `*UiModel.kt` — the render target the Composable reads. No domain types as field types.
- `*UiMapper.kt` — a single top-level extension function mapping `*State` to `*UiModel`. This is the **only** place that boundary is crossed.
- `*StoreFactory.kt` — contains the `Bootstrapper`, `Executor`, and a nested `internal object ReducerImpl` (never a standalone `*Reducer.kt` file, and never `private` — `internal` visibility is what lets a reducer be unit-tested directly from `commonTest`).
- `Route.kt` / `Screen.kt` / `ViewModel.kt` — same responsibilities as the MVI convention below.
- `component/` — a subfolder for composables reused *within this screen only* (e.g. `screen/main/component/PokemonListItem.kt`). A composable extracted for reuse takes a `UiModel`, not raw primitives. Something reused *across* screens belongs in `app/design/component/` instead.

**The boundary is enforced at the Composable's input type**: `*Screen.kt` and `ViewModel.state` only ever expose `*UiModel`, never `*State`. `*State` is free to hold domain objects; `*UiModel` never does.

### MVI conventions
- `*Intent` — user-initiated events from the UI
- `*Label` — one-shot side effects (navigation, etc.) — a screen with nothing to signal (like Detail) still declares an empty sealed interface, it isn't omitted
- `*Action` — bootstrapper-initiated internal triggers
- `*Message` — reducer input, produced by the executor
- `*State` — immutable, screen-logical state snapshot; the input the reducer reads and writes

### Navigation (Navigation 3)
- Each feature defines its own `*Navigator` interface and `*NavKeyHandler` — a feature only ever knows its own destinations.
- `app/navigation/impl/` holds the concrete `*NavigatorImpl` classes that wire features together.
- `infra/navigation/` owns feature-agnostic plumbing (`AppNavigator`, `NavGraph`) with zero feature imports. `AppNavigator` wraps a lazily-`attach()`-ed `NavBackStack` — calls made before attachment are no-ops, not crashes.

### Error handling
- `AppError` is a single sealed interface; `AppException` is the only throw/catch mechanism.
- Error-to-display resolution happens at a shared `AppError -> AppErrorUiModel` mapping (`common/error/AppErrorUiMapper.kt`), reusing `UiText` (`infra/ui/UiText.kt`) for any value that depends on runtime data rather than only a static resource.
- **No hardcoded UI strings, ever** — every user-facing string is a Compose Multiplatform resource in `shared/src/commonMain/composeResources/values/strings.xml`. Naming convention: `<screen_or_domain>_<element>_<role>`, snake_case (e.g. `pokemon_main_fab_description`, `pokemon_detail_height_label`, `error_network_unavailable_title`) — grouped by feature/concern with a blank line between groups, not alphabetized.

### Dependency injection (Koin)
- `factoryOf` for UseCases and StoreFactories, `viewModelOf` for ViewModels, `singleOf` for Repositories and DataSources — **except** when the constructor has a defaulted parameter that shouldn't be resolved from the DI graph.
- **Gotcha that has caused a real production crash in this repo**: `singleOf(::Impl)` uses reflection and resolves **every** constructor parameter from the DI graph — including ones with Kotlin default values. It does not skip them. If a class has a defaulted parameter (e.g. the `currentTimeMillis` lambda above) that has no binding, `singleOf` will crash at resolution time trying to find one. Use an explicit lambda instead so Kotlin's own default-parameter mechanism applies: `single<Interface> { Impl(get(), get()) }` (note the missing third argument — Koin never touches it, in a bare function call).
- Root aggregation (`AppModule.kt`) and navigation wiring (`NavigationModule.kt`) live in `app/`, separate from each feature's own `di/` module.

### Architecture & coverage enforcement (Konsist)
Rules live in `konsistTest/`, one file per rule *category* (not per layer), so each test file answers one question regardless of which layer or file suffix is involved. Two Konsist API pitfalls worth knowing before adding a new rule:
- `KoFileDeclaration.name` **excludes** the `.kt` extension — comparing it against `"Foo.kt"` will never match.
- `KoFileDeclaration.path` uses the OS-native path separator (backslash on Windows) — a hardcoded `"/foo/"` check will silently misbehave cross-platform. Prefer `resideInPath(...)`, `resideInSourceSet(...)`, `withNameEndingWith(...)`, and `withPackage(...)` over raw string `.filter { }` — they handle this internally.

`TestingTest.kt` specifically enforces that every `Mapper`, `RepositoryImpl`, `DataSourceImpl`, `UseCase`, `UiMapper`, and `StoreFactory` (as a `ReducerTest` + `ExecutorTest` pair) has a corresponding test file — **if you add a new one of these without a test, the build turns red.** Keep this list in sync as new production-file categories get established.

### Testing conventions
- No mocking library anywhere in the project. Seams are either hand-written fakes (file-local `private class` for one-off data-source/API tests, or the single shared `Fake<Feature>Repository` per feature living in `domain/fake/` for anything reused across multiple test files) or an injected lambda (the `currentTimeMillis` pattern above).
- `runTest` wraps every suspend-based test, even ones with no real async work — it's a blanket convention, not reserved for cases that need it.
- Turbine (`app.cash.turbine.test`) is reserved specifically for MVI `labels` (one-shot events). `StateFlow` state is asserted with a plain synchronous read after `testDispatcher.scheduler.runCurrent()`/`advanceTimeBy(...)` — don't reach for Turbine there.
- The one integration-style test in the suite (`PokemonLocalDataSourceImplTest`, in `androidHostTest`) uses a real in-memory SQLDelight driver rather than a fake, since it exists specifically to prove the SQL itself (ordering, autoincrement, `Flow` reactivity) is correct — something a fake can't do.

---

## Local Setup

This project uses **Husky** and **Commitlint** to enforce conventional commit messages.

### Prerequisites

- [Node.js](https://nodejs.org/) (includes npm)

### Initial Setup

After forking or cloning the project, run the following command in the root directory to install the commit linting tools and initialize the Git hooks:

```bash
npm install
```

### Commit Message Convention

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification.

**Format:** `<type>(<scope>): <description>`

- **Types:** `feat`, `fix`, `refactor`, `build`, `chore`, `ci`, `docs`, `perf`, `style`, `test`, `revert`.
- **Scopes:** `network`, `database`, `di`, `navigation`, `theme`, `common`, `gradle`, `deps`, `feature-a`, `feature-b`, `pokemon-explorer`.
- **Note:** A scope is **required** for types: `feat`, `fix`, `refactor`, and `build`.

> [!IMPORTANT]
> `feature-a`, `feature-b`, and `pokemon-explorer` are all template/example scopes. Once you fork this project, remove them from `commitlint.config.js` and add scopes for your own feature(s) instead — see [CLAUDE.md](CLAUDE.md) for the full forking checklist.

Example of a valid commit:
`feat(common): add new utility function`

---

## 🧪 Running Tests

```bash
# Run architecture + test-coverage rules
./gradlew :konsistTest:test

# Run unit tests (data, domain, presentation, one SQLDelight integration test)
./gradlew :shared:testAndroidHostTest

# Run ktlint across every source set
./gradlew ktlintCheck
```

---

## 🧑‍💻 Author

**Nicolas Zurbuchen**
Android Software Engineer based in Tokyo, Japan
Contact: [nicolas.zurbuchen@outlook.com](mailto:nicolas.zurbuchen@outlook.com)
