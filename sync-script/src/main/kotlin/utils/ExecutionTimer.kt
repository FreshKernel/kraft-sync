package utils

import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

/**
 * A helper class to know for how long a task has been running.
 * */
class ExecutionTimer {
    private var startTime: Duration? = null

    fun setStartTime() {
        startTime = System.nanoTime().nanoseconds
    }

    fun getRunningUntilNowDuration(): Duration {
        startTime?.let { startTime ->
            return System.nanoTime().nanoseconds - startTime
        }
        throw IllegalStateException(
            "The ${getRunningUntilNowDuration()::class.simpleName}() has been called without calling ${setStartTime()::class.simpleName}()",
        )
    }
}
