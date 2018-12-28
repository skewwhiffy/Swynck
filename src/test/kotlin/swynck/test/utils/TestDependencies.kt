package swynck.test.utils

import swynck.app.Dependencies
import swynck.daemon.DaemonRunner
import swynck.daemon.DaemonTaskStatus
import swynck.daemon.task.DaemonTask
import swynck.db.Migrations

class TestDependencies : Dependencies(
    TestConfig(),
    TestDaemonRunner()
) {
    init { Migrations(this).run() }
}

class TestDaemonRunner : DaemonRunner {
    override fun add(task: DaemonTask) { }

    override fun statusOf(task: DaemonTask): DaemonTaskStatus? = null
}