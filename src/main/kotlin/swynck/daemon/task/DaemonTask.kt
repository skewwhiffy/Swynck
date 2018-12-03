package swynck.daemon.task

import java.time.Duration

interface DaemonTask {
    suspend fun runSingle(): Unit
    val restartPolicy: RestartPolicy
}

interface RestartPolicy

object NoRestart : RestartPolicy

class Restart(pause: Duration) : RestartPolicy