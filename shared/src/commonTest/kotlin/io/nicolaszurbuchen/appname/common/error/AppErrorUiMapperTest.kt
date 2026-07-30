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
import kotlin.test.Test
import kotlin.test.assertEquals

class AppErrorUiMapperTest {
    @Test
    fun toUiModel_networkUnavailable_mapsToWifiOffIconWithUnavailableText() {
        val result = AppError.Network.Unavailable.toUiModel()

        assertEquals(
            AppErrorUiModel(
                title = UiText.Resource(Res.string.error_network_unavailable_title),
                subtitle = UiText.Resource(Res.string.error_network_unavailable_subtitle),
                icon = Icons.Outlined.WifiOff,
            ),
            result,
        )
    }

    @Test
    fun toUiModel_networkTimeout_mapsToWifiOffIconWithTimeoutText() {
        val result = AppError.Network.Timeout.toUiModel()

        assertEquals(
            AppErrorUiModel(
                title = UiText.Resource(Res.string.error_network_timeout_title),
                subtitle = UiText.Resource(Res.string.error_network_timeout_subtitle),
                icon = Icons.Outlined.WifiOff,
            ),
            result,
        )
    }

    @Test
    fun toUiModel_networkHttpWithServerMessage_usesServerMessageAsRawSubtitle() {
        val result = AppError.Network.Http(code = 500, serverMessage = "Internal error").toUiModel()

        assertEquals(
            AppErrorUiModel(
                title = UiText.Resource(Res.string.error_network_http_title),
                subtitle = UiText.Raw("Internal error"),
                icon = Icons.Outlined.WifiOff,
            ),
            result,
        )
    }

    @Test
    fun toUiModel_networkHttpWithoutServerMessage_fallsBackToDefaultSubtitle() {
        val result = AppError.Network.Http(code = 500, serverMessage = null).toUiModel()

        assertEquals(
            AppErrorUiModel(
                title = UiText.Resource(Res.string.error_network_http_title),
                subtitle = UiText.Resource(Res.string.error_network_http_subtitle_default),
                icon = Icons.Outlined.WifiOff,
            ),
            result,
        )
    }

    @Test
    fun toUiModel_databaseQueryFailed_mapsToStorageIcon() {
        val result = AppError.Database.QueryFailed(RuntimeException("boom")).toUiModel()

        assertEquals(
            AppErrorUiModel(
                title = UiText.Resource(Res.string.error_database_query_failed_title),
                subtitle = UiText.Resource(Res.string.error_database_generic_subtitle),
                icon = Icons.Outlined.Storage,
            ),
            result,
        )
    }

    @Test
    fun toUiModel_databaseInsertFailed_mapsToStorageIcon() {
        val result = AppError.Database.InsertFailed(RuntimeException("boom")).toUiModel()

        assertEquals(
            AppErrorUiModel(
                title = UiText.Resource(Res.string.error_database_insert_failed_title),
                subtitle = UiText.Resource(Res.string.error_database_generic_subtitle),
                icon = Icons.Outlined.Storage,
            ),
            result,
        )
    }

    @Test
    fun toUiModel_pokemonExplorerFetchFailed_mapsToWifiOffIcon() {
        val result = AppError.PokemonExplorer.FetchFailed.toUiModel()

        assertEquals(
            AppErrorUiModel(
                title = UiText.Resource(Res.string.error_pokemon_fetch_failed_title),
                subtitle = UiText.Resource(Res.string.error_pokemon_fetch_failed_subtitle),
                icon = Icons.Outlined.WifiOff,
            ),
            result,
        )
    }

    @Test
    fun toUiModel_unexpected_mapsToErrorOutlineIcon() {
        val result = AppError.Unexpected(RuntimeException("boom")).toUiModel()

        assertEquals(
            AppErrorUiModel(
                title = UiText.Resource(Res.string.error_unexpected_title),
                subtitle = UiText.Resource(Res.string.error_unexpected_subtitle),
                icon = Icons.Outlined.ErrorOutline,
            ),
            result,
        )
    }
}
