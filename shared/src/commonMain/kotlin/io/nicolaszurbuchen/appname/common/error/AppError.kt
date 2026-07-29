package io.nicolaszurbuchen.appname.common.error

sealed interface AppError {
    sealed interface Network : AppError {
        data object Unavailable : Network

        data object Timeout : Network

        data class Http(
            val code: Int,
            val serverMessage: String? = null,
        ) : Network
    }

    sealed interface Database : AppError {
        data class QueryFailed(
            val cause: Throwable,
        ) : Database

        data class InsertFailed(
            val cause: Throwable,
        ) : Database
    }

    sealed interface PokemonExplorer : AppError {
        data object FetchFailed : PokemonExplorer
    }

    data class Unexpected(
        val cause: Throwable,
    ) : AppError
}
