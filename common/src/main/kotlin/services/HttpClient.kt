package services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import utils.executeAsync
import utils.getBodyOrThrow
import java.time.Duration

object HttpClient {
    val okHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(Duration.ofMinutes(2))
            .callTimeout(Duration.ofHours(2))
            .readTimeout(Duration.ofHours(1))
            .writeTimeout(Duration.ofMinutes(30))
            .retryOnConnectionFailure(true)
            .build()

    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
    ): HttpResponse {
        return try {
            require(url.isNotEmpty()) { "The HTTP request URL can't be empty" }
            var requestBuilder: Request.Builder =
                Request
                    .Builder()
                    .url(url)
            if (headers.isNotEmpty()) {
                requestBuilder = requestBuilder.apply { headers.forEach { (name, value) -> addHeader(name, value) } }
            }
            val request = requestBuilder.get().build()

            return okHttpClient.newCall(request).executeAsync().use {
                withContext(Dispatchers.IO) {
                    val response = it
                    return@withContext when (response.isSuccessful) {
                        true -> {
                            val responseBody = response.getBodyOrThrow()
                            HttpResponse.Success(body = responseBody.string())
                        }

                        false -> {
                            val httpError =
                                when (response.code) {
                                    HttpError.NotFound.statusCode -> HttpError.NotFound
                                    HttpError.Forbidden.statusCode -> HttpError.Forbidden
                                    HttpError.InternalServer.statusCode -> HttpError.InternalServer
                                    else -> {
                                        val errorMessage = "Unhandled HTTP status code: ${response.code}"
                                        return@withContext HttpResponse.UnknownError(
                                            errorMessage = errorMessage,
                                            exception = IllegalStateException(errorMessage),
                                        )
                                    }
                                }
                            HttpResponse.HttpFailure(
                                httpStatusMessage = response.message,
                                httpError = httpError,
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            HttpResponse.UnknownError(errorMessage = e.toString(), exception = e)
        }
    }
}
