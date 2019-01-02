package swynck.test.utils

import swynck.app.Dependencies
import swynck.app.DependenciesImpl
import swynck.common.defaultRedirectUri
import swynck.daemon.DaemonRunner
import swynck.daemon.DaemonTaskStatus
import swynck.daemon.task.DaemonTask
import swynck.db.Migrations
import swynck.fake.onedrive.FakeOnedriveClients
import swynck.real.onedrive.client.OnedriveWrapper
import swynck.test.util.TestConfig

class TestDependencies : Dependencies by DependenciesImpl(
    TestConfig(),
    TestDaemonRunner()
) {
    init { Migrations(this).run() }

    override val onedriveClients = FakeOnedriveClients()
    override val oneDrive = OnedriveWrapper(onedriveClients, config)

    fun addValidUser() {
        val accessToken = oneDrive.getAccessToken(onedriveClients.authCode, config.defaultRedirectUri())
        val user = oneDrive.getUser(accessToken, config.defaultRedirectUri())
        userRepository.addUser(user)
    }
}

fun Dependencies.with(onedriveClient: OnedriveWrapper) = object : Dependencies by this {
    override val oneDrive = onedriveClient
}

class TestDaemonRunner : DaemonRunner {
    override fun add(task: DaemonTask) { }

    override fun statusOf(task: DaemonTask): DaemonTaskStatus? = null
}