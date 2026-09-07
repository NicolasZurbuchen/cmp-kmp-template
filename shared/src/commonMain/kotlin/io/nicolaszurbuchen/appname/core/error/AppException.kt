package io.nicolaszurbuchen.appname.common.error

class AppException(
    val error: AppError,
) : Exception("App error: $error")
