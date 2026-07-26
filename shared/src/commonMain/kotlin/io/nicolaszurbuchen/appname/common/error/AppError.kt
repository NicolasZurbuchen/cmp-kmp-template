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

    sealed interface Trivia : AppError {
        data object NoResults : Trivia

        data object InvalidParameter : Trivia

        data object RateLimit : Trivia
    }

    data class Unexpected(
        val cause: Throwable,
    ) : AppError
}

fun Int.toAppError(): AppError =
    when (this) {
        1 -> AppError.Trivia.NoResults
        2 -> AppError.Trivia.InvalidParameter
        5 -> AppError.Trivia.RateLimit
        else -> AppError.Network.Http(this)
    }
