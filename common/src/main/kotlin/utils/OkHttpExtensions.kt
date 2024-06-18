package utils

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.internal.closeQuietly
import java.io.IOException
import kotlin.coroutines.resumeWithException

fun Response.getBodyOrThrow(message: String? = null): ResponseBody {
    return requireNotNull(this.body) {
        message ?: "GET request to: ${request.body} returned no response body (null)"
    }
}

// TODO: Use https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp-coroutines later

/**
 * @author https://github.com/square/okhttp/blob/54238b4c713080c3fd32fb1a070fb5d6814c9a09/okhttp-coroutines/src/main/kotlin/okhttp3/coroutines/ExecuteAsync.kt#L29
 * */
suspend fun Call.executeAsync(): Response =
    suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            this.cancel()
        }
        this.enqueue(
            object : Callback {
                override fun onFailure(
                    call: Call,
                    e: IOException,
                ) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(
                    call: Call,
                    response: Response,
                ) {
                    continuation.resume(response) { _, _, _ ->
                        response.closeQuietly()
                    }
                }
            },
        )
    }
