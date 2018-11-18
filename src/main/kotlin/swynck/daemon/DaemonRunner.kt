package swynck.daemon

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import org.h2.mvstore.ConcurrentArrayList
import swynck.daemon.task.DaemonTask
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentSkipListSet

class DaemonRunner(private val clock: Clock) {
    private var runnerTask: Deferred<Nothing>? = null
    private val running = ConcurrentLinkedQueue<Deferred<*>>()
    val exceptions = ConcurrentArrayList<Exception>()

    private val taskKeys = ConcurrentHashMap<UUID, DaemonTask>()
    private val tasksAndRuns = ConcurrentHashMap<UUID, ConcurrentSkipListSet<Instant>>()

    fun add(task: DaemonTask) {
        val key = UUID.randomUUID()
        taskKeys[key] = task
        tasksAndRuns[key] = ConcurrentSkipListSet()
        @Synchronized
        runnerTask = runnerTask ?: async { start() }
    }

    private suspend fun start(): Nothing {
        while (true) {
            running
                .filter { it.isCompleted }
                .let { running.removeAll(it) }
            tasksAndRuns.keys.forEach { taskKey ->
                val now = clock.instant()
                val oneMinuteAgo = now.minus(Duration.ofMinutes(1))
                tasksAndRuns[taskKey]!!
                    .removeAll(tasksAndRuns[taskKey]!!.filter { it < oneMinuteAgo })
                if (tasksAndRuns[taskKey]!!.size < taskKeys[taskKey]!!.runsPerMinute) {
                    running.add(async {
                        try {
                            taskKeys[taskKey]!!.run()
                        } catch (e: Exception) {
                            exceptions.add(e)
                        }
                    })
                    tasksAndRuns[taskKey]!!.add(now)
                }
            }
        }
    }
}