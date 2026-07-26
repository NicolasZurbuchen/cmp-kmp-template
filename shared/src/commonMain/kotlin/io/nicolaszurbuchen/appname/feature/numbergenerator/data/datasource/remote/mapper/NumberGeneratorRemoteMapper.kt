package io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.mapper

import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.dto.NumberFactDto

fun NumberFactDto.toValue(): String? = text.takeIf { found }
