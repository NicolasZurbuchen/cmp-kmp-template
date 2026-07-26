package io.nicolaszurbuchen.appname.common.error

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.WifiOff
import io.nicolaszurbuchen.appname.infra.ui.UiText

fun AppError.toUiModel(): AppErrorUiModel =
    when (this) {
        is AppError.Network.Unavailable -> {
            AppErrorUiModel(
                title = UiText.Raw("No internet connection"),
                subtitle = UiText.Raw("Please check your network settings."),
                icon = Icons.Outlined.WifiOff,
            )
        }

        is AppError.Network.Timeout -> {
            AppErrorUiModel(
                title = UiText.Raw("Request timed out"),
                subtitle = UiText.Raw("The server took too long to respond."),
                icon = Icons.Outlined.WifiOff,
            )
        }

        is AppError.Network.Http -> {
            AppErrorUiModel(
                title = UiText.Raw("Network error"),
                subtitle = serverMessage?.let { UiText.Raw(it) } ?: UiText.Raw("Something went wrong. Try again."),
                icon = Icons.Outlined.WifiOff,
            )
        }

        is AppError.Database.QueryFailed -> {
            AppErrorUiModel(
                title = UiText.Raw("Couldn't read from storage"),
                subtitle = UiText.Raw("Something went wrong. Try again."),
                icon = Icons.Outlined.Storage,
            )
        }

        is AppError.Database.InsertFailed -> {
            AppErrorUiModel(
                title = UiText.Raw("Couldn't save to storage"),
                subtitle = UiText.Raw("Something went wrong. Try again."),
                icon = Icons.Outlined.Storage,
            )
        }

        is AppError.PokemonExplorer.FetchFailed -> {
            AppErrorUiModel(
                title = UiText.Raw("Couldn't fetch a Pokémon"),
                subtitle = UiText.Raw("Check your connection and try again."),
                icon = Icons.Outlined.WifiOff,
            )
        }

        is AppError.Unexpected -> {
            AppErrorUiModel(
                title = UiText.Raw("Something went wrong"),
                subtitle = UiText.Raw("Please try again."),
                icon = Icons.Outlined.ErrorOutline,
            )
        }
    }
