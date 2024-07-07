package services

sealed class HttpError(
    val statusCode: Int,
) {
    data object NotFound : HttpError(statusCode = 404)

    data object Forbidden : HttpError(statusCode = 403)

    data object InternalServer : HttpError(statusCode = 500)
}
