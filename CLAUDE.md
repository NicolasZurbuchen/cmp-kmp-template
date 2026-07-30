# CLAUDE.md

Guidance for Claude Code (or any agent) working in this repository.

## What this repository is

A Kotlin Multiplatform / Compose Multiplatform (Android + iOS) application template. It ships with one complete example feature, **pokemon-explorer**, whose only purpose is to demonstrate every layer and convention end to end — it is not the point of the project. The point is the architecture, the Konsist rules that enforce it, and the test-writing conventions, all of which are meant to survive being forked into a real project.

[README.md](README.md) explains the project to a *human* — what it is, the tech stack, how to set it up. It is not the technical source of truth for an agent. That lives in **`agents/`**, read in full before doing any non-trivial work here:

- [`agents/agent-architecture-convention.md`](agents/agent-architecture-convention.md) — package placement decision procedure, layer shape, MVI vocabulary, DI, error handling, testing conventions.
- [`agents/agent-commit-convention.md`](agents/agent-commit-convention.md) — deterministic `type`/`scope` selection for commit messages.
- [`agents/agent-design-system-convention.md`](agents/agent-design-system-convention.md) — the color/typography/spacing layer model under `app/design/theme/`.

This file covers what those three don't: the fork checklist, and a short list of judgment calls that came up during this template's own development and aren't derivable from reading the code once.

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
6. **Commit scopes, in three places that must all agree**: `commitlint.config.js`'s `scope-enum`, the `Scopes:` bullet in `README.md`, and the "Template scopes" section at the bottom of `agents/agent-commit-convention.md`. Remove the template scopes `feature-a`, `feature-b`, and `pokemon-explorer`, and add a scope for your actual feature(s) instead. (`pokemon-explorer` is the example feature's scope — same fate as `feature-a`/`feature-b` once you're past the example.) These three files have drifted out of sync with each other before in this exact repo — don't let it happen again.
7. **Decide what to do with `feature/pokemonexplorer/`**: either delete it outright (and its wiring: the `pokemonExplorerModule` entry in `app/di/AppModule.kt`, `PokemonExplorerNavigatorImpl.kt`, the initial route in `app/navigation/NavConfig.kt`) once you understand the pattern well enough to build your own feature from scratch, or keep it temporarily as a working side-by-side reference while you build your first real feature, then delete it once you're confident. Either is fine — just don't ship it as-is in a "finished" fork.
8. **What you do NOT need to touch**: `konsistTest/src/test/kotlin/io/nicolaszurbuchen/appname/PackageHierarchyTest.kt`'s allowed top-level package list (`app`, `common`, `feature`, `infra`) is structural, not name-specific — it doesn't care what your root package is called, only what comes right after it. Same for every other Konsist rule in this repo except the ones that hardcode `pokemon-explorer`-specific names (there aren't any — all rules key off suffix/package shape, not literal feature names).

---

## Judgment calls not obvious from the code alone

Everything structural, deterministic, or repeatable already lives in `agents/agent-architecture-convention.md` and is enforced by Konsist — it isn't restated here. What follows is the handful of things that came up as real corrections during this template's development and are genuinely easy to get wrong once, not the kind of thing you can re-derive from the code or from Konsist failing.

- **Compose call sites**: `Modifier` is always the **last** argument at the call site, not just last in the function signature (trailing lambdas aside). Nothing currently enforces this mechanically — it's a review-time check.
- **Don't add defensive nullability**: don't make a field or parameter nullable "just in case" if the call site is only ever reachable with a valid value given how the layer above maps things. Nullability should describe a real, reachable state, not hedge against a scenario the codebase already prevents.
- **Magic numbers get a comment, not just a name**: a named constant with no explanation just moves the "why this bound?" question one file over instead of answering it.
- **Gradle version catalog bundles**: if three or more libraries are always added together (see `ktor-common`, `compose-common`, `mvikotlin-common` in `[bundles]`), define a bundle and consume it via `libs.bundles.x` instead of listing each one at every call site.
- **Package placement decision criteria** (also in `agents/agent-architecture-convention.md`, repeated here because it's the single most load-bearing judgment call in this codebase): does this file know about a specific feature? If it knows about **multiple** features → `app/`. If it knows about exactly **one** feature → `feature/<name>/`. If it knows about **zero** features and is pure technical plumbing → `infra/`. If it knows about zero features but is shared domain vocabulary (not plumbing) → `common/`. Don't pre-emptively put something in `common/` because it *might* be shared later — wait until a second feature actually needs it.

---

## Verification before calling anything done

```bash
./gradlew :konsistTest:test              # architecture + test-coverage rules
./gradlew :shared:testAndroidHostTest    # unit tests (commonTest + androidHostTest)
./gradlew ktlintCheck                    # formatting, across every source set — not just the one you touched
```

Run all three, not just the test you think is relevant — `ktlintCheck` in particular has a history in this repo of catching violations across files a narrower, filtered test run never touches.

If a Konsist rule fails and you're tempted to change the rule to make it pass: don't. See `agents/agent-architecture-convention.md`'s last section.
