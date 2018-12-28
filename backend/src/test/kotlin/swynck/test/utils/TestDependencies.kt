package swynck.test.utils

import swynck.app.Dependencies
import swynck.app.DependenciesImpl
import swynck.daemon.DaemonRunner
import swynck.daemon.DaemonTaskStatus
import swynck.daemon.task.DaemonTask
import swynck.db.Migrations
import swynck.service.Onedrive

class TestDependencies : Dependencies by DependenciesImpl(
    TestConfig(),
    TestDaemonRunner()
) {
    init { Migrations(this).run() }
}

fun Dependencies.with(onedrive: Onedrive) = object : Dependencies by this {
    override val oneDrive = onedrive
}

class TestDaemonRunner : DaemonRunner {
    override fun add(task: DaemonTask) { }

    override fun statusOf(task: DaemonTask): DaemonTaskStatus? = null
}