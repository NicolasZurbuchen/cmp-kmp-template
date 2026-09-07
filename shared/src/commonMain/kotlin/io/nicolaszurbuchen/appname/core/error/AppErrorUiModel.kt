package io.nicolaszurbuchen.appname.common.error

import androidx.compose.ui.graphics.vector.ImageVector
import io.nicolaszurbuchen.appname.infra.ui.UiText

data class AppErrorUiModel(
    val title: UiText,
    val subtitle: UiText,
    val icon: ImageVector,
)
