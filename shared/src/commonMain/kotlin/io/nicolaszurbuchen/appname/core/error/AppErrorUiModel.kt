package io.nicolaszurbuchen.appname.core.error

import androidx.compose.ui.graphics.vector.ImageVector
import io.nicolaszurbuchen.appname.infra.text.UiText

data class AppErrorUiModel(
    val title: UiText,
    val subtitle: UiText,
    val icon: ImageVector,
)
