package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NumberFactDto(
    val text: String,
    val number: Int,
    val found: Boolean,
    val type: String,
)
