package swynck.daemon

import org.h2.mvstore.ConcurrentArrayList
import swynck.daemon.task.DaemonTask

class DaemonRunner {
    private val tasks = ConcurrentArrayList<DaemonTask>()

    fun add(task: DaemonTask) {
        tasks.add(task)
    }
}