package swynck.daemon

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.h2.mvstore.ConcurrentArrayList
import swynck.daemon.task.DaemonTask
import java.time.Duration
import java.time.Instant
import kotlin.streams.asStream

class DaemonRunner {
    private val runnerTask: Deferred<Nothing>
    init {
        runnerTask = async {
            start()
        }
    }

    private val tasks = ConcurrentArrayList<DaemonTask>()

    fun add(task: DaemonTask) {
        tasks.add(task)
    }

    private suspend fun start(): Nothing {
        var lastRun = Instant.now()
        while (true) {
            val nextRun = lastRun.plusSeconds(1)
            val now = Instant.now()
            if (nextRun > now) {
                val wait = Duration.between(now, nextRun)
                delay(wait.toMillis())
            }
            val runs = tasks.iterator().asSequence().map { async { it.run() } }
            // TODO: There must be a nicer way to do this
            while (runs.any { it.isActive }) {
                delay(50)
            }
            lastRun = Instant.now()
        }
    }
}