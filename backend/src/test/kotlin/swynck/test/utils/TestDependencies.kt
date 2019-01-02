package swynck.test.utils

import swynck.app.Dependencies
import swynck.common.defaultRedirectUri
import swynck.common.time.Ticker
import swynck.daemon.DaemonRunner
import swynck.daemon.DaemonTaskStatus
import swynck.daemon.task.DaemonTask
import swynck.db.DataSourceFactory
import swynck.db.Migrations
import swynck.db.OnedriveMetadataRepository
import swynck.db.UserRepository
import swynck.fake.onedrive.FakeOnedriveClients
import swynck.real.onedrive.client.OnedriveWrapper
import swynck.test.util.TestConfig
import java.time.Duration

class TestDependencies : Dependencies {
    override val config = TestConfig()
    override val daemonRunner = TestDaemonRunner()
    override val dataSourceFactory = DataSourceFactory(config)
    override val metadata = OnedriveMetadataRepository(dataSourceFactory)
    override val ticker = TestTicker()
    override val onedriveClients = FakeOnedriveClients()
    override val oneDrive = OnedriveWrapper(onedriveClients, config)
    override val userRepository = UserRepository(dataSourceFactory)

    init { Migrations(this).run() }

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

class TestTicker : Ticker {
    override suspend fun delay(duration: Duration) { }
}