# cmp-kmp-template

A Kotlin Multiplatform / Compose Multiplatform application template targeting Android and iOS. (`AppName` is the internal placeholder project/package name used throughout the code — not this repository's name — and gets renamed to your real project's name when you fork it; see the Forking Checklist in [CLAUDE.md](CLAUDE.md).) It ships with one complete, fully-tested example feature — **pokemon-explorer** (fetch a random Pokémon, browse fetch history, view detail) — built against the public [PokéAPI](https://pokeapi.co/), which exists purely to demonstrate every architectural convention end to end: data/domain/presentation layering, MVI, DI wiring, navigation, error handling, and a full unit + architecture test suite.

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

Four top-level packages, each with a distinct responsibility: `app/` composes the whole application and is the only place allowed to know about more than one feature at once; `infra/` is reusable technical plumbing with zero domain or feature knowledge; `common/` holds domain concepts genuinely shared across features; `feature/` holds vertical feature slices, each owning its full data/domain/presentation stack and never reaching into another feature's internals.

Each feature follows Clean Architecture layering with an MVI presentation layer (MVIKotlin's Store/Executor/Reducer), a strict `State` (internal) vs. `UiModel` (what the Composable actually renders) split, Koin for dependency injection, Navigation 3 for routing, and a matching Konsist rule for nearly every convention mentioned above — this repo treats "documented but not enforced" as equivalent to "not true."

For the full, precise rule set — the decision procedure for where a new file goes, the exact shape every layer and MVI file must take, DI/testing/error-handling conventions, and the Konsist gotchas worth knowing before touching an architecture test — see [`agents/agent-architecture-convention.md`](agents/agent-architecture-convention.md). It's written for an agent to follow deterministically, but it's the same document a new human contributor should read too.

---

## Setup & Commit Conventions

This project uses **Husky** and **Commitlint** to enforce [Conventional Commits](https://www.conventionalcommits.org/) at commit time. After forking or cloning, install the Node tooling once ([Node.js](https://nodejs.org/) required) to activate the Git hooks:

```bash
npm install
```

**Format:** `<type>(<scope>): <description>`

- **Types:** `feat`, `fix`, `refactor`, `build`, `chore`, `ci`, `docs`, `perf`, `style`, `test`, `revert`.
- **Scopes:** `network`, `database`, `di`, `navigation`, `theme`, `common`, `gradle`, `deps`, `feature-a`, `feature-b`, `pokemon-explorer`.
- A scope is **required** for `feat`, `fix`, `refactor`, and `build`.

Example: `feat(common): add new utility function`

> [!IMPORTANT]
> `feature-a`, `feature-b`, and `pokemon-explorer` are all template/example scopes. Once you fork this project, remove them from `commitlint.config.js` and add scopes for your own feature(s) instead. See [CLAUDE.md](CLAUDE.md) for the full forking checklist — this scope list, `agents/agent-commit-convention.md`, and this README have drifted out of sync with each other before, so update all three together.

For the full deterministic `type`/`scope` decision procedure (not just the format), see [`agents/agent-commit-convention.md`](agents/agent-commit-convention.md).

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
