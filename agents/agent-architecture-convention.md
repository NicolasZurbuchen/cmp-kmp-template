# Agent Architecture Convention

This file exists so an agent can place a new file in the right package and shape a new screen or feature correctly, without re-deriving the architecture from scratch or asking a human. The concrete example throughout is the `pokemon-explorer` feature; the pattern is what's fixed, not that specific feature or its file count.

## Package placement — decision procedure

Walk this in order for any new file:

1. Does it know about **more than one** feature? → `app/`
2. Does it know about **exactly one** feature? → `feature/<name>/`
3. Does it know about **zero** features, and is it pure technical plumbing with no domain vocabulary? → `infra/`
4. Does it know about **zero** features, but represents domain vocabulary shared across features? → `common/`

Don't place something in `common/` speculatively because it *might* be reused later. A single-feature project has almost nothing there — wait until a second feature actually needs the same domain concept before promoting it out of the first one.

## Shape of `app/`, `common/`, and `infra/`

```
app/
├── App.kt                       # Root Composable: theme + image loader + NavGraph
├── design/
│   ├── component/               # App-wide reusable composables (e.g. AppErrorBanner) — cross-feature; a component shared only within one feature belongs in feature/<name>/presentation/component/ instead, and one used by only a single screen belongs in feature/<name>/presentation/screen/<screen>/component/
│   └── theme/                   # Design tokens, color palette, spacing, typography — see agent-design-system-convention.md
├── di/
│   └── AppModule.kt             # Aggregates every feature/infra Koin module into one list — the only DI file allowed to know about more than one feature
└── navigation/
    ├── impl/                    # Concrete *NavigatorImpl classes — the only place allowed to know about more than one feature's destinations at once
    ├── NavConfig.kt
    └── NavigationModule.kt

common/
└── error/                       # AppError / AppException, single throw-catch mechanism — a single-feature project has little else here; it only grows when a second feature needs to share the same domain concept as the first

infra/
├── database/                    # SQLDelight driver setup (expect/actual)
├── mvi/                         # MVIKotlin base wiring (StoreFactory binding)
├── navigation/                  # AppNavigator, NavKeyHandler, NavGraph — feature-agnostic, zero feature imports
├── network/                     # Ktor client configuration (expect/actual engine)
├── platform/                    # expect/actual platform utilities (BackHandler, Platform)
└── ui/                          # UiText — resource/raw/composite text abstraction
```

## Layer shape inside a feature

```
feature/<name>/
├── data/
│   ├── datasource/
│   │   ├── local/                    # *LocalDataSource(Impl); a mapper/ subfolder holds only top-level extension functions mapping the local storage type to a domain model
│   │   └── remote/                   # *RemoteDataSource(Impl); an api/ subfolder holds the Ktor *Api(Impl), a dto/ subfolder the wire-format *Dto classes, and a mapper/ subfolder the top-level extension functions mapping Dto -> domain model
│   ├── repository/                   # *RepositoryImpl only, implements the domain interface
│   └── di/
├── domain/
│   ├── model/                        # pure values — no Flow/StateFlow, no internal mutability
│   ├── repository/                   # interface only, no default implementations
│   └── usecase/                      # reserved for logic that touches a port (repository, clock) or coordinates more than one step; a UseCase never injects another UseCase
└── presentation/
    ├── navigation/                   # *Destination, *Navigator (interface), *NavKeyHandler — a feature only ever knows its own destinations
    ├── component/                    # composables reused across screens *within this feature only*; take a UiModel, never raw primitives. Cross-feature reuse goes in app/design/component/ instead
    └── screen/<screen>/
        ├── *Contract.kt              # exactly Intent/Label/Action/Message (sealed interfaces) + State (data class) — nothing else lives here
        ├── *StoreFactory.kt          # Bootstrapper + Executor + a nested `internal object ReducerImpl` — never a standalone *Reducer.kt file, never `private` (internal is what makes it directly unit-testable from commonTest)
        ├── *UiMapper.kt               # a single top-level extension function, State -> UiModel — the only place that conversion happens
        ├── *UiModel.kt                # the Composable's actual input type — no domain types as field types
        ├── *ViewModel.kt              # wraps the StoreFactory; exposes `state: StateFlow<*UiModel>` and `labels: Flow<*Label>` — never State
        ├── *Route.kt / *Screen.kt     # the Screen's public function takes only Modifier, the matching *UiModel, or lambdas — never *State
        └── component/                 # composables reused only within this screen; take a UiModel, never raw primitives
```

## MVI vocabulary

- `Intent` — a user-initiated event from the UI
- `Label` — a one-shot side effect (navigation, etc.) — a screen with genuinely nothing to signal still declares an empty sealed interface, it isn't omitted
- `Action` — a bootstrapper-initiated internal trigger
- `Message` — reducer input, produced by the executor
- `State` — an immutable, screen-logical snapshot the reducer reads and writes — **never leaves the Store/Executor/Reducer/UiMapper boundary**. If a Composable seems to need a State field, that means the UiMapper is missing a field, not a reason to pass State through. This is enforced by Konsist (`PresentationLayerTest.kt`), not just convention.

## Dependency injection (Koin)

- `factoryOf` — UseCases, StoreFactories
- `viewModelOf` — ViewModels
- `singleOf` — Repositories, DataSources, **only when every constructor parameter should be resolved from the DI graph**

`singleOf(::Impl)` resolves every constructor parameter via reflection, including ones with Kotlin default values — it does not skip them. A class with a defaulted parameter that must *not* come from the DI graph (e.g. an injected clock lambda kept as a default for deterministic testing) needs an explicit binding instead: `single<Interface> { Impl(get(), get()) }`. Omitting the argument lets Kotlin's own default apply, since Koin never touches a parameter it wasn't asked to resolve.

## Error handling

- `AppError` (a single sealed interface) plus `AppException` is the only throw/catch mechanism in the app — no ad-hoc exception types.
- Display resolution happens once, at a shared `AppError -> AppErrorUiModel` mapping (`common/error/AppErrorUiMapper.kt`), using `UiText` (`infra/ui/UiText.kt`) for anything that needs runtime data rather than only a static resource.
- Every user-facing string is a Compose Multiplatform resource (`shared/src/commonMain/composeResources/values/strings.xml`) — never a hardcoded literal in a Composable. Naming: `feature_screen_role`, or `common_role` for a cross-feature string. Add an `element` segment only when `role` alone would be ambiguous within that screen. Group entries by feature/concern with a blank line between groups, not alphabetically.

## Testing

- No mocking library. A seam is either a hand-written fake — file-local `private class` for a one-off data-source/API test, or a single shared `Fake<Feature>Repository` in `domain/fake/` when it's reused across several test files for the same feature — or an injected lambda (the clock pattern above).
- `runTest` wraps every suspend-based test, unconditionally, even ones with no real async work.
- Turbine is for `Label` flows only (one-shot events). `StateFlow` state is read synchronously after `testDispatcher.scheduler.runCurrent()` / `advanceTimeBy(...)`.
- Every `Mapper`, `RepositoryImpl`, `DataSourceImpl`, `UseCase`, `UiMapper`, and `StoreFactory` (as a matching `ReducerTest` + `ExecutorTest` pair) needs a corresponding test file — enforced by `konsistTest/TestingTest.kt`, not just this document. Add to that file's coverage list when a new category of production file gets established.

## Konsist rules — do not modify without asking

`konsistTest/` is the enforcement mechanism for every rule above. Do not loosen, delete, or work around a Konsist rule to make a change compile or a build go green. If a rule is genuinely too strict for something you're legitimately trying to do, stop and tell the developer why, and let them decide whether the rule should change — don't decide that yourself. Silently editing the rule to fit the code defeats the entire point of having it.
