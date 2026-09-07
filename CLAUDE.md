# CLAUDE.md

Guidance for Claude Code (or any agent) working in this repository.

## What this repository is

A Kotlin Multiplatform / Compose Multiplatform (Android + iOS) application template. It ships with one complete example feature, **pokemon-explorer**, whose only purpose is to demonstrate every layer and convention end to end — it is not the point of the project. The point is the architecture, the Konsist rules that enforce it, and the test-writing conventions, all of which are meant to survive being forked into a real project.

[README.md](README.md) explains the project to a *human* — what it is, the tech stack, how to set it up. It is not the technical source of truth for an agent. That lives in **`agents/`**, read in full before doing any non-trivial work here:

- [`agents/agent-architecture-convention.md`](agents/agent-architecture-convention.md) — package placement decision procedure, layer shape, MVI vocabulary, previews, loading states, DI, error handling, testing conventions.
- [`agents/agent-commit-convention.md`](agents/agent-commit-convention.md) — deterministic `type`/`scope` selection for commit messages.
- [`agents/agent-design-system-convention.md`](agents/agent-design-system-convention.md) — the color/typography/spacing/shimmer layer model under `design/theme/`.
- [`agents/agent-documentation-convention.md`](agents/agent-documentation-convention.md) — **where prose goes.** Which declarations get KDoc, when a `//` comment earns its place, and what belongs in `DECISIONS.md` instead of in a file.

[`DECISIONS.md`](DECISIONS.md) is that fourth document's destination: why the app is the way it is, including the alternatives that were rejected. It ships nearly empty — the entries in it are this template's own decisions, and a fork adds its product's underneath.

This file covers what those don't: the fork checklist, and a short list of judgment calls that came up during this template's own development and aren't derivable from reading the code once.

---

## Is this repo still the unforked template?

Check `settings.gradle.kts`'s `rootProject.name` and any Kotlin file's package declaration. If they're still `AppName` / `io.nicolaszurbuchen.appname`, nobody has forked this yet. If a user asks you to build their own project on top of this template and it's still in this state, run through the **Forking Checklist** below (or point them at it) before writing feature code — otherwise the new feature gets built on top of placeholder naming that will need an awkward rename later.

---

## Forking Checklist

Everything below is about renaming the placeholder identity (`AppName` / `io.nicolaszurbuchen.appname`) to the real project's. **Use your IDE's "Rename Package" refactor for the Kotlin package rename** (Android Studio: right-click the root package → Refactor → Rename) rather than a manual find-and-replace — it updates file paths, imports, and generated-resource references consistently in one pass; a text-level find-and-replace on a KMP project this size is easy to get subtly wrong (e.g. missing a file under `iosMain`).

1. **Kotlin package**: rename `io.nicolaszurbuchen.appname` → `<your.reverse.domain>.<yourapp>` across `shared/`, `androidApp/`, and `konsistTest/`. This includes:
   - `androidApp/src/main/kotlin/.../AppNameApplication.kt` → rename the class itself too, and update the reference in `AndroidManifest.xml`'s `android:name=".YourApplication"`.
   - `design/theme/Theme.kt`'s `AppNameTheme` composable and `design/preview/AppNamePreview.kt` — rename both to match. They're leftover placeholder names, not a generic convention, and `konsistTest/PreviewTest.kt` asserts the preview harness by name, so rename it there too.
2. **Project/module names**:
   - `settings.gradle.kts`: `rootProject.name = "AppName"` → your project name.
   - `androidApp/build.gradle.kts`: `namespace` and `applicationId` (currently both `io.nicolaszurbuchen.appname`).
   - `shared/build.gradle.kts`: `namespace = "io.nicolaszurbuchen.appname.shared"`, and the SQLDelight `packageName.set("io.nicolaszurbuchen.appname.cache")`.
3. **Android display name**: `androidApp/src/main/res/values/strings.xml`'s `app_name` string (currently literally `"AppName"`).
4. **iOS**: `iosApp/Configuration/Config.xcconfig` — `PRODUCT_NAME` and `PRODUCT_BUNDLE_IDENTIFIER` (currently `io.nicolaszurbuchen.appname.AppName$(TEAM_ID)`). Renaming the `iosApp` Xcode project/folder itself is optional and more involved (Xcode project renames are fragile) — changing these two values is enough to fix the display name and bundle id without touching the project structure.
5. **Compose Multiplatform generated resources** (`appname.shared.generated.resources`): this package is derived automatically from the module/namespace at build time — **don't try to touch it manually**, just rebuild after steps 1–2 and it regenerates under the new name.
6. **Commit scopes, in three places that must all agree**: `commitlint.config.js`'s `scope-enum`, the `Scopes:` bullet in `README.md`, and the "Template scopes" section at the bottom of `agents/agent-commit-convention.md`. Remove the template scopes `feature-a`, `feature-b`, and `pokemon-explorer`, and add a scope for your actual feature(s) instead. (`pokemon-explorer` is the example feature's scope — same fate as `feature-a`/`feature-b` once you're past the example.) A CI job lints every commit message on a pull request, so a scope that exists in one file and not the other now fails the build rather than drifting quietly.
7. **Set the CI branch**: `.github/workflows/verify.yml` triggers on pushes to `master`. Change it if the fork's default branch is named something else.
8. **Decide what to do with `feature/pokemonexplorer/`**: either delete it outright (and its wiring: the `pokemonExplorerModule` entry in `app/di/AppModule.kt`, `PokemonExplorerNavigatorImpl.kt`, the initial route in `app/navigation/NavConfig.kt`) once you understand the pattern well enough to build your own feature from scratch, or keep it temporarily as a working side-by-side reference while you build your first real feature, then delete it once you're confident. Either is fine — just don't ship it as-is in a "finished" fork.
9. **What you do NOT need to touch**: `konsistTest/src/test/kotlin/io/nicolaszurbuchen/appname/PackageHierarchyTest.kt`'s allowed top-level package list (`app`, `core`, `design`, `feature`, `infra`) is structural, not name-specific — it doesn't care what your root package is called, only what comes right after it. Same for every other Konsist rule in this repo except the preview-harness name in step 1.

---

## Judgment calls not obvious from the code alone

Everything structural, deterministic, or repeatable already lives in `agents/agent-architecture-convention.md` and is enforced by Konsist — it isn't restated here. What follows is the handful of things that came up as real corrections and are genuinely easy to get wrong once.

- **Compose call sites**: `Modifier` is always the **last** argument at the call site, not just last in the function signature (trailing lambdas aside). Nothing currently enforces this mechanically — it's a review-time check.
- **Don't add defensive nullability**: don't make a field or parameter nullable "just in case" if the call site is only ever reachable with a valid value given how the layer above maps things. Nullability should describe a real, reachable state, not hedge against a scenario the codebase already prevents.
- **Magic numbers get a comment, not just a name**: a named constant with no explanation just moves the "why this bound?" question one file over instead of answering it.
- **Prose is routed, not sprinkled**: a KDoc says what a caller needs to call it correctly, a `//` says why the *code* is surprising, and why the *app* is like this goes in `DECISIONS.md` — even when the code it justifies is right there. The same explanation appearing twice always means something is wrong, but read the code under it before deciding what: either the abstraction is missing and wants extracting, or it already exists and the prose was written past it. See `agents/agent-documentation-convention.md`.
- **Gradle version catalog bundles**: if three or more libraries are always added together (see `ktor-common`, `compose-common`, `mvikotlin-common` in `[bundles]`), define a bundle and consume it via `libs.bundles.x` instead of listing each one at every call site.
- **Typography slots are roles, not Material's scale.** Every slot in `design/theme/Type.kt` carries a comment naming what it's for *here*; `titleLarge` may well be a 14sp button label. Picking by Material's semantics instead of by the comment is how a screen ends up rendering its headline at caption size — and it compiles, so nothing catches it.
- **A new Konsist rule is not trusted until it has been seen to fail.** Point it at a real violation, watch it go red, revert. A rule that cannot fail is worse than no rule because it reads as coverage: this repo has shipped a tautological rule, two exclusions naming packages that never existed, and four allow-list entries for packages nobody had written.
- **Package placement decision criteria** (also in `agents/agent-architecture-convention.md`, repeated because it's the single most load-bearing judgment call here). Five top-level packages, and **every question is answerable by reading the file itself** — that's the property that makes the procedure work, and the reason it doesn't ask how many features use something: "is it cross-feature" is a fact about the rest of the system, and it can become true without the file changing.
  1. Does it **compose the app** — wire the graph, own the navigation host? → `app/`, which is terminal: it imports everything and nothing imports it.
  2. Exactly **one** feature? → `feature/<name>/`
  3. Does it **model the subject** — a domain type, or the rendering of one? → `core/`
  4. **Tokens, or a component whose contract is purely presentational?** → `design/`
  5. **Pure plumbing, no domain vocabulary, no brand?** → `infra/`
- **`design/` versus `core/<slice>/presentation/`** is the one call that list doesn't settle: does the component own a rule about *the subject*, or only about *appearance*? A filter chip decides nothing about meaning and is `design/`; a timeline that decides how a day lays out owns a rule about the subject and belongs beside the model. The linguistic form of the same test is whether the `App` prefix reads sensibly — `AppFilterChip` does, `AppInvoiceTimeline` doesn't.

---

## Verification before calling anything done

```bash
./gradlew :konsistTest:test
```

```bash
./gradlew :shared:testAndroidHostTest
```

```bash
./gradlew ktlintCheck
```

```bash
./gradlew :shared:verifyCommonMainAppDatabaseMigration
```

Run all four, not just the one you think is relevant — `ktlintCheck` in particular has a history in this repo of catching violations across files a narrower, filtered test run never touches.

The migration check is the odd one out and the reason it's on this list: **a table added to a `.sq` file without a matching `.sqm` compiles, runs, and passes every other command here.** It only breaks on a device that already had the database, because SQLDelight takes the schema version from the migration files rather than the schema files. Regenerate the snapshot with `:shared:generateCommonMainAppDatabaseSchema` whenever a `.sq` file changes, and commit the `.db` it writes.

`:konsistTest:test` does not need `--rerun-tasks`. It used to: Konsist builds its scopes from strings at runtime, so Gradle could not see that `:shared`'s sources are this task's real inputs and marked it UP-TO-DATE, replaying the last result. A violation added to `:shared` left the command green. The task is now pinned to always run — see the comment in `konsistTest/build.gradle.kts`. If you find `--rerun-tasks` in an old note or habit, it's stale rather than wrong.

All four run in CI on every push and pull request, alongside an iOS job that compiles the framework and runs `commonTest` on Native, and an Android job that assembles and lints the app. Running them locally first is still faster than waiting for a red build.

If a Konsist rule fails and you're tempted to change the rule to make it pass: don't. See `agents/agent-architecture-convention.md`'s last section.
