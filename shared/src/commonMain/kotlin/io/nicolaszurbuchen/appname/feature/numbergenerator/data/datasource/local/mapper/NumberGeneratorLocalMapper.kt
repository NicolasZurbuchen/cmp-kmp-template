package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local.mapper

import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local.GeneratedNumberEntity
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.model.GeneratedNumber

fun GeneratedNumberEntity.toDomain(): GeneratedNumber =
    GeneratedNumber(
        id = id,
        // SQLDelight escapes the `value` column to `value_` because `value` is a Kotlin soft keyword.
        value = value_.toInt(),
        fact = fact,
        createdAt = created_at,
        isFavorite = is_favorite == 1L,
        isSynced = is_synced == 1L,
    )
