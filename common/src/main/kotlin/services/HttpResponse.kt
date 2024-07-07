package services

import kotlinx.serialization.json.Json
import utils.JsonIgnoreUnknownKeys

sealed class HttpResponse {
    data class Success(
        val body: String,
    ) : HttpResponse() {
        inline fun <reified T> decodeJson(ignoreUnknownKeys: Boolean = true): T =
            (if (ignoreUnknownKeys) JsonIgnoreUnknownKeys else Json).decodeFromString(body)
    }

    data class HttpFailure(
        val httpStatusMessage: String,
        val httpError: HttpError,
    ) : HttpResponse() {
        fun exception(): IllegalStateException = IllegalStateException("HTTP Error (${httpError.statusCode}) $httpStatusMessage")
    }

    data class UnknownError(
        val errorMessage: String,
        val exception: Exception,
    ) : HttpResponse()
}
