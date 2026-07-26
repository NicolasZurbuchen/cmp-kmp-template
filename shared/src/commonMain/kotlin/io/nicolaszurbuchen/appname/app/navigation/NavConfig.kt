package io.nicolaszurbuchen.appname.app.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.navigation.DetailDestination
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.navigation.MainDestination
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val navConfig =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(MainDestination::class)
                    subclass(DetailDestination::class)
                }
            }
    }
