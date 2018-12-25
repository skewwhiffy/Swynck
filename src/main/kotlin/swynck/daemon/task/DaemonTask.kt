package swynck.daemon.task

import java.time.Duration

interface DaemonTask {
    suspend fun runSingle()
    val restartPolicy: RestartPolicy
}

interface RestartPolicy

object NoRestart : RestartPolicy

class Restart(val pause: Duration) : RestartPolicy