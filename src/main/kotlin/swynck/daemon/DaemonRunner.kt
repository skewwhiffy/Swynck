package swynck.daemon

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import org.h2.mvstore.ConcurrentArrayList
import swynck.daemon.task.DaemonTask
import swynck.daemon.task.NoRestart
import java.time.Clock
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class DaemonRunner(private val clock: Clock) {
    val exceptions = ConcurrentArrayList<Exception>()

    private val taskKeys = ConcurrentHashMap<UUID, DaemonTask>()
    private val taskRuns = ConcurrentHashMap<UUID, Deferred<*>>()
    private val taskStatus = ConcurrentHashMap<UUID, DaemonTaskStatus>()

    fun add(task: DaemonTask) {
        val key = UUID.randomUUID()
        taskKeys[key] = task
        val run = async { runContinuously(task) }
        taskRuns[key] = run
        taskStatus[key] = Running(listOf())
    }

    val tasks get() = taskKeys.values.toList()

    fun statusOf(task: DaemonTask): DaemonTaskStatus? {
        return taskKeys
            .keys
            .singleOrNull() { taskKeys[it] == task }
            ?.let { taskStatus[it] }
    }

    private suspend fun runContinuously(task: DaemonTask) {
        while(true) {
            try {
                task.runSingle()
            } catch (e: Exception) {
                when (task.restartPolicy) {
                    NoRestart -> {
                        val key = taskKeys.keys.single { taskKeys[it] == task }
                        val currentStatus = taskStatus[key]
                        taskStatus[key] = when (currentStatus) {
                            is Running -> Errored(listOf(e))
                            else -> TODO()
                        }
                        return
                    }
                    else -> TODO()
                }
            }
        }
    }
}

interface DaemonTaskStatus {
    val exceptions: List<Exception>
}

class Running(override val exceptions: List<Exception>) : DaemonTaskStatus

class Errored(override val exceptions: List<Exception>) : DaemonTaskStatus
