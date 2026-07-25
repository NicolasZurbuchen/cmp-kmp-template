package io.nicolaszurbuchen.appname.infra.network

import io.ktor.client.engine.HttpClientEngine

expect fun httpClientEngine(): HttpClientEngine
