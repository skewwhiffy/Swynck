package swynck.daemon.task

interface DaemonTask {
    suspend fun runSingle(): Unit
}