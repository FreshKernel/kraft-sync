package utils

import okhttp3.OkHttpClient
import java.time.Duration

object HttpService {
    val client =
        OkHttpClient.Builder()
            .connectTimeout(Duration.ofMinutes(2))
            .callTimeout(Duration.ofHours(2))
            .readTimeout(Duration.ofHours(1))
            .writeTimeout(Duration.ofMinutes(30))
            .retryOnConnectionFailure(true)
            .build()
}
