package io.nicolaszurbuchen.appname.common.error

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.WifiOff
import appname.shared.generated.resources.Res
import appname.shared.generated.resources.error_database_generic_subtitle
import appname.shared.generated.resources.error_database_insert_failed_title
import appname.shared.generated.resources.error_database_query_failed_title
import appname.shared.generated.resources.error_network_http_subtitle_default
import appname.shared.generated.resources.error_network_http_title
import appname.shared.generated.resources.error_network_timeout_subtitle
import appname.shared.generated.resources.error_network_timeout_title
import appname.shared.generated.resources.error_network_unavailable_subtitle
import appname.shared.generated.resources.error_network_unavailable_title
import appname.shared.generated.resources.error_pokemon_fetch_failed_subtitle
import appname.shared.generated.resources.error_pokemon_fetch_failed_title
import appname.shared.generated.resources.error_unexpected_subtitle
import appname.shared.generated.resources.error_unexpected_title
import io.nicolaszurbuchen.appname.infra.ui.UiText

fun AppError.toUiModel(): AppErrorUiModel =
    when (this) {
        is AppError.Network.Unavailable -> {
            AppErrorUiModel(
                title = UiText.Resource(Res.string.error_network_unavailable_title),
                subtitle = UiText.Resource(Res.string.error_network_unavailable_subtitle),
                icon = Icons.Outlined.WifiOff,
            )
        }

        is AppError.Network.Timeout -> {
            AppErrorUiModel(
                title = UiText.Resource(Res.string.error_network_timeout_title),
                subtitle = UiText.Resource(Res.string.error_network_timeout_subtitle),
                icon = Icons.Outlined.WifiOff,
            )
        }

        is AppError.Network.Http -> {
            AppErrorUiModel(
                title = UiText.Resource(Res.string.error_network_http_title),
                subtitle =
                    serverMessage?.let { UiText.Raw(it) }
                        ?: UiText.Resource(Res.string.error_network_http_subtitle_default),
                icon = Icons.Outlined.WifiOff,
            )
        }

        is AppError.Database.QueryFailed -> {
            AppErrorUiModel(
                title = UiText.Resource(Res.string.error_database_query_failed_title),
                subtitle = UiText.Resource(Res.string.error_database_generic_subtitle),
                icon = Icons.Outlined.Storage,
            )
        }

        is AppError.Database.InsertFailed -> {
            AppErrorUiModel(
                title = UiText.Resource(Res.string.error_database_insert_failed_title),
                subtitle = UiText.Resource(Res.string.error_database_generic_subtitle),
                icon = Icons.Outlined.Storage,
            )
        }

        is AppError.PokemonExplorer.FetchFailed -> {
            AppErrorUiModel(
                title = UiText.Resource(Res.string.error_pokemon_fetch_failed_title),
                subtitle = UiText.Resource(Res.string.error_pokemon_fetch_failed_subtitle),
                icon = Icons.Outlined.WifiOff,
            )
        }

        is AppError.Unexpected -> {
            AppErrorUiModel(
                title = UiText.Resource(Res.string.error_unexpected_title),
                subtitle = UiText.Resource(Res.string.error_unexpected_subtitle),
                icon = Icons.Outlined.ErrorOutline,
            )
        }
    }
