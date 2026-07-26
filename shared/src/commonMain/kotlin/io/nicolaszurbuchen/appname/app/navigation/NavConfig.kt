package io.nicolaszurbuchen.appname.app.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.navigation.GenerateDestination
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.navigation.HistoryDestination
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val navConfig =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(GenerateDestination::class)
                    subclass(HistoryDestination::class)
                }
            }
    }
