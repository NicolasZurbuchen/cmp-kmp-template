package io.nicolaszurbuchen.appname

import io.nicolaszurbuchen.appname.infra.platform.getPlatform

class Greeting {
    private val platform = getPlatform()

    fun greet(): String = sayHello(platform.name)
}
