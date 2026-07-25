package io.nicolaszurbuchen.appname

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
