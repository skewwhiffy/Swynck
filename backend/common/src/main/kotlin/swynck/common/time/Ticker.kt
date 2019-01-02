package swynck.common.time

import kotlinx.coroutines.delay
import java.time.Duration

interface Ticker {
    companion object {
        operator fun invoke() = TickerImpl()
    }

    suspend fun delay(duration: Duration)
}

class TickerImpl : Ticker {
    override suspend fun delay(duration: Duration) = delay(duration.toMillis())
}