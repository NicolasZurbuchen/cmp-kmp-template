package io.nicolaszurbuchen.appname.feature.numbergenerator.domain.model

data class GeneratedNumber(
    val id: Long,
    val value: Int,
    val fact: String?,
    val createdAt: Long,
    val isFavorite: Boolean,
    val isSynced: Boolean,
)
