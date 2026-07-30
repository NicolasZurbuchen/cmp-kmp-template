# CLAUDE.md

Guidance for Claude Code (or any agent) working in this repository.

## What this repository is

A Kotlin Multiplatform / Compose Multiplatform (Android + iOS) application template. It ships with one complete example feature, **pokemon-explorer**, whose only purpose is to demonstrate every layer and convention end to end — it is not the point of the project. The point is the architecture, the Konsist rules that enforce it, and the test-writing conventions, all of which are meant to survive being forked into a real project.

Read [README.md](README.md) first — specifically its **🏛 Architecture Decisions** section — for the full description of the layering, MVI, navigation, DI, and testing conventions. This file covers what README doesn't: the fork checklist, and the "why" behind decisions that aren't visible just from reading the code.

---

## Is this repo still the unforked template?

Check `settings.gradle.kts`'s `rootProject.name` and any Kotlin file's package declaration. If they're still `AppName` / `io.nicolaszurbuchen.appname`, nobody has forked this yet. If a user asks you to build their own project on top of this template and it's still in this state, run through the **Forking Checklist** below (or point them at it) before writing feature code — otherwise the new feature gets built on top of placeholder naming that will need an awkward rename later.

---

## Forking Checklist

Everything below is about renaming the placeholder identity (`AppName` / `io.nicolaszurbuchen.appname`) to the real project's. **Use your IDE's "Rename Package" refactor for the Kotlin package rename** (Android Studio: right-click the root package → Refactor → Rename) rather than a manual find-and-replace — it updates file paths, imports, and generated-resource references consistently in one pass; a text-level find-and-replace on a KMP project this size is easy to get subtly wrong (e.g. missing a file under `iosMain`).

1. **Kotlin package**: rename `io.nicolaszurbuchen.appname` → `<your.reverse.domain>.<yourapp>` across `shared/`, `androidApp/`, and `konsistTest/`. This includes:
   - `androidApp/src/main/kotlin/.../AppNameApplication.kt` → rename the class itself too, and update the reference in `AndroidManifest.xml`'s `android:name=".YourApplication"`.
   - `app/design/theme/Theme.kt`'s `AppNameTheme` composable — rename to match, it's currently a leftover placeholder name, not a generic convention.
2. **Project/module names**:
   - `settings.gradle.kts`: `rootProject.name = "AppName"` → your project name.
   - `androidApp/build.gradle.kts`: `namespace` and `applicationId` (currently both `io.nicolaszurbuchen.appname`).
   - `shared/build.gradle.kts`: `namespace = "io.nicolaszurbuchen.appname.shared"`, and the SQLDelight `packageName.set("io.nicolaszurbuchen.appname.cache")`.
3. **Android display name**: `androidApp/src/main/res/values/strings.xml`'s `app_name` string (currently literally `"AppName"`).
4. **iOS**: `iosApp/Configuration/Config.xcconfig` — `PRODUCT_NAME` and `PRODUCT_BUNDLE_IDENTIFIER` (currently `io.nicolaszurbuchen.appname.AppName$(TEAM_ID)`). Renaming the `iosApp` Xcode project/folder itself is optional and more involved (Xcode project renames are fragile) — changing these two values is enough to fix the display name and bundle id without touching the project structure.
5. **Compose Multiplatform generated resources** (`appname.shared.generated.resources`): this package is derived automatically from the module/namespace at build time — **don't try to touch it manually**, just rebuild after steps 1–2 and it regenerates under the new name.
6. **`commitlint.config.js`**: remove the template scopes `feature-a`, `feature-b`, and `pokemon-explorer` from `scope-enum`, and add a scope for your actual feature(s) instead. (`pokemon-explorer` is the example feature's scope — same fate as `feature-a`/`feature-b` once you're past the example.)
7. **`README.md`**: update the `Scopes:` bullet under Commit Message Convention to match whatever you set in step 6 — these two have drifted out of sync before in this exact repo, don't let it happen again.
8. **Decide what to do with `feature/pokemonexplorer/`**: either delete it outright (and its wiring: the `pokemonExplorerModule` entry in `app/di/AppModule.kt`, `PokemonExplorerNavigatorImpl.kt`, the initial route in `app/navigation/NavConfig.kt`) once you understand the pattern well enough to build your own feature from scratch, or keep it temporarily as a working side-by-side reference while you build your first real feature, then delete it once you're confident. Either is fine — just don't ship it as-is in a "finished" fork.
9. **What you do NOT need to touch**: `konsistTest/src/test/kotlin/io/nicolaszurbuchen/appname/PackageHierarchyTest.kt`'s allowed top-level package list (`app`, `common`, `feature`, `infra`) is structural, not name-specific — it doesn't care what your root package is called, only what comes right after it. Same for every other Konsist rule in this repo except the ones that hardcode `pokemon-explorer`-specific names (there aren't any — all rules key off suffix/package shape, not literal feature names).

---

## Commit conventions

Full detail in [README.md](README.md#commit-message-convention). The two things worth knowing beyond what's written there:

- `scope-required-on-types` is a **custom** commitlint rule (see `commitlint.config.js`), not part of `@commitlint/config-conventional` — it's what makes a scope mandatory specifically for `feat`/`fix`/`refactor`/`build` (other types like `chore`/`docs`/`style` can omit one).
- The scope enum is a hardcoded allowlist, not inferred from the codebase — when you add a new feature, add its scope to `commitlint.config.js` *and* update the README's documented list in the same commit. See step 6–7 of the Forking Checklist above for the fork-time version of this same rule.

---

## Design rules that aren't obvious from the code alone

These came out of real corrections during this template's own development — the kind of thing that's easy to reintroduce by accident if you're not told about it up front.

**Compose call sites**
- `Modifier` is always the **last** argument at the call site, not just last in the function signature (trailing lambdas aside). Getting the signature right but the call site wrong is exactly the kind of thing that slips through review.

**MVI / State boundary**
- A screen's reducer is a nested `internal object ReducerImpl` inside its `*StoreFactory.kt`, never a standalone `*Reducer.kt` file. `internal` (not `private`) is required — it's what lets `<Screen>ReducerTest` call it directly from `commonTest`.
- `*Screen.kt` and `ViewModel.state` expose **only** `*UiModel`, never `*State`. If you're tempted to pass `*State` to a Composable "just this once," that's the signal the `*UiMapper` is missing a field, not a reason to skip it.
- A reusable list-item-style composable takes a `UiModel`, not a handful of raw `String`/`Int` parameters — wrap them first.

**Koin**
- Don't inject `Random` (or anything similarly "just call it inline" simple) through the DI graph — call `Random.nextInt(...)` directly where it's used. Do inject non-deterministic *time* as a defaulted constructor lambda (`currentTimeMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() }`) so tests can pin it — but see the next point before wiring it with `singleOf`.
- `singleOf(::Impl)` resolves **every** constructor parameter via reflection, including ones with Kotlin default values — it does not skip them, and this has caused a real crash in this repo (`InstanceCreationException` at app launch, no binding found for a defaulted lambda parameter). If a class has a defaulted parameter that shouldn't be pulled from the DI graph, bind it with an explicit lambda instead: `single<Interface> { Impl(get(), get()) }` — leaving the third argument out lets Kotlin's own default apply, since Koin never touches a parameter you didn't ask it to resolve.

**Data layer**
- Don't make a field/parameter nullable defensively "just in case" — if the call site is only ever reachable with a valid value because of how the layer above maps things, the type should say so. Nullability should describe a real, reachable state, not hedge against a scenario the codebase already prevents.
- SQLDelight: use an autoincrement row id as the primary key whenever the same real-world entity can legitimately reappear (e.g. a fetch-history table) — a natural/business key silently overwrites instead of accumulating.
- Any "magic number" constant gets a name **and** a comment saying where the bound came from. A constant with no explanation just moves the "why 1025?" question one file over instead of answering it.

**Strings**
- No hardcoded UI-facing strings, anywhere — every one goes in `shared/src/commonMain/composeResources/values/strings.xml`. Naming: `<screen_or_domain>_<element>_<role>`, snake_case, grouped by feature with a blank line between groups (not alphabetized) — e.g. `pokemon_detail_height_label`, `error_network_unavailable_title`.

**Gradle version catalog**
- `gradle/libs.versions.toml` is organized into commented sections by concern (Kotlin/Compose, Android, Navigation, DI, Networking/Images, Persistence, Architecture/MVI, Lint/Static Analysis/Testing) — new entries go in the matching section, not tacked onto the end.
- If three or more libraries are always added together (see `ktor-common`, `compose-common`, `mvikotlin-common` in `[bundles]`), define a bundle and consume it via `libs.bundles.x` instead of listing each one at every call site.

**Konsist**
- `KoFileDeclaration.name` excludes the `.kt` extension; comparing it against a string ending in `.kt` will silently never match anything. `KoFileDeclaration.path` uses the OS-native separator (backslash on Windows) — a hardcoded `"/foo/"` substring check breaks cross-platform. Use `resideInPath(...)`, `resideInSourceSet(...)`, `withNameEndingWith(...)`, `withPackage(...)` instead of raw string filtering — the existing rule files (`DataLayerTest.kt`, `DomainLayerTest.kt`, etc.) are the reference for this style.
- This project's Android target uses the newer `com.android.kotlin.multiplatform.library` AGP plugin, whose local-JVM-test source set is named **`androidHostTest`**, not the classic KMP `androidUnitTest`. Kotlin Gradle Plugin doesn't generate a typed dot-accessor for it yet, so wiring a dependency into it needs `getByName("androidHostTest").dependencies { ... }` instead of the usual `androidHostTest.dependencies { ... }`.
- `TestingTest.kt` enforces that every `Mapper`, `RepositoryImpl`, `DataSourceImpl`, `UseCase`, `UiMapper`, and `StoreFactory` (as a matching `ReducerTest` + `ExecutorTest` pair) has a corresponding test file. If you add a new one of these kinds of files, this suite goes red until you write its test — that's intentional, not a bug to route around.

**Package placement decision criteria** (see README's Architecture Decisions for the full writeup)
- Ask: does this file know about a specific feature? If it knows about **multiple** features → `app/`. If it knows about exactly **one** feature → `feature/<name>/`. If it knows about **zero** features and is pure technical plumbing → `infra/`. If it knows about zero features but is shared domain vocabulary (not plumbing) → `common/`.
- Don't pre-emptively put something in `common/` because it *might* be shared later — wait until a second feature actually needs it.

---

## Verification before calling anything done

```bash
./gradlew :konsistTest:test              # architecture + test-coverage rules
./gradlew :shared:testAndroidHostTest    # unit tests (commonTest + androidHostTest)
./gradlew ktlintCheck                    # formatting, across every source set — not just the one you touched
```

Run all three, not just the test you think is relevant — `ktlintCheck` in particular has a history in this repo of catching violations across files a narrower, filtered test run never touches.
