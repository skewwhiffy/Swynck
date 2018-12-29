package swynck.daemon

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import swynck.daemon.task.DaemonTask
import swynck.daemon.task.NoRestart
import swynck.daemon.task.Restart
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.EmptyCoroutineContext

interface DaemonRunner {
    companion object {
        operator fun invoke() = DaemonRunnerImpl()
    }

    fun add(task: DaemonTask)
    fun statusOf(task: DaemonTask): DaemonTaskStatus?
}

class DaemonRunnerImpl : DaemonRunner {
    private val scope = CoroutineScope(EmptyCoroutineContext)
    private val taskKeys = ConcurrentHashMap<UUID, DaemonTask>()
    private val taskRuns = ConcurrentHashMap<UUID, Deferred<*>>()
    private val taskStatus = ConcurrentHashMap<UUID, DaemonTaskStatus>()

    override fun add(task: DaemonTask) {
        val key = UUID.randomUUID()
        taskKeys[key] = task
        val run = scope.async { runContinuously(task) }
        taskRuns[key] = run
        taskStatus[key] = Running(listOf())
    }

    override fun statusOf(task: DaemonTask): DaemonTaskStatus? {
        return taskKeys
            .keys
            .singleOrNull { taskKeys[it] == task }
            ?.let { taskStatus[it] }
    }

    private suspend fun runContinuously(task: DaemonTask) {
        while(true) {
            try {
                task.runSingle()
            } catch (e: Exception) {
                val policy = task.restartPolicy
                when (policy) {
                    NoRestart -> {
                        val key = taskKeys.keys.single { taskKeys[it] == task }
                        val currentStatus = taskStatus[key]
                        taskStatus[key] = when (currentStatus) {
                            is Running -> Errored(listOf(e))
                            else -> throw InvalidStateException
                        }
                        return
                    }
                    is Restart -> {
                        val key = taskKeys.keys.single { taskKeys[it] == task }
                        val currentStatus = taskStatus[key]
                        taskStatus[key] = when (currentStatus) {
                            is Running -> RunningWithErrors(listOf(e))
                            is RunningWithErrors -> RunningWithErrors(currentStatus.exceptions + e)
                            else -> throw InvalidStateException
                        }
                        delay(policy.pause.toMillis())
                    }
                    else -> throw InvalidStateException
                }
            }
        }
    }
}

object InvalidStateException : Exception()

interface DaemonTaskStatus {
    val exceptions: List<Exception>
}

class Running(override val exceptions: List<Exception>) : DaemonTaskStatus

class RunningWithErrors(override val exceptions: List<Exception>) : DaemonTaskStatus

class Errored(override val exceptions: List<Exception>) : DaemonTaskStatus
