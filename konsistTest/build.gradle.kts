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
}
