plugins {
    kotlin("jvm")
    alias(libs.plugins.ktlint)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation(libs.konsist)
    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnitPlatform()

    // Konsist reads the *other* modules' Kotlin sources, and Gradle has no way to know that: the
    // scopes are built at runtime from strings (`scopeFromProduction("shared")`,
    // `scopeFromProject()`), so nothing in this module's declared inputs changes when a file in
    // `:shared` moves. Gradle therefore marks this task UP-TO-DATE and replays the previous result.
    //
    // That is not a slow build, it is a false pass: adding a forbidden import to a file in
    // `:shared` leaves `./gradlew :konsistTest:test` green, while the same command with
    // `--rerun-tasks` fails. The flag has been the workaround; this makes it unnecessary, because
    // a rule nobody remembers to force is a rule that is not enforced.
    //
    // Declaring the real inputs would be the surgical fix and was rejected: `scopeFromProject()`
    // means "every .kt file in the repo", which is a fileTree over the root at configuration time
    // — awkward under the configuration cache, and silently wrong again the day a scope widens.
    outputs.upToDateWhen { false }
}
