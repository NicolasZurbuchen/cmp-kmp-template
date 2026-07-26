package io.nicolaszurbuchen.appname.infra.platform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
