package swynck.daemon

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import org.h2.mvstore.ConcurrentArrayList
import swynck.daemon.task.DaemonTask
import java.time.Clock
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class DaemonRunner(private val clock: Clock) {
    val exceptions = ConcurrentArrayList<Exception>()

    private val taskKeys = ConcurrentHashMap<UUID, DaemonTask>()
    private val taskRuns = ConcurrentHashMap<UUID, Deferred<*>>()

    fun add(task: DaemonTask) {
        val key = UUID.randomUUID()
        taskKeys[key] = task
        val run = async { task.run() }
        taskRuns[key] = run
    }
}