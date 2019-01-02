package swynck.app

import swynck.common.Config
import swynck.common.time.Ticker
import swynck.daemon.DaemonRunner
import swynck.db.DataSourceFactory
import swynck.db.OnedriveMetadataRepository
import swynck.db.UserRepository
import swynck.real.onedrive.client.OnedriveClients
import swynck.real.onedrive.client.OnedriveClientsImpl
import swynck.real.onedrive.client.OnedriveWrapper

// TODO: Proper IoC container
interface Dependencies {
    companion object {
        operator fun invoke() = DependenciesImpl()
    }
    val config: Config
    val dataSourceFactory: DataSourceFactory
    val daemonRunner: DaemonRunner
    val metadata: OnedriveMetadataRepository
    val ticker: Ticker
    val onedriveClients: OnedriveClients
    val oneDrive: OnedriveWrapper
    val userRepository: UserRepository
}

class DependenciesImpl(
    override val config: Config = Config(),
    override val daemonRunner: DaemonRunner = DaemonRunner()
): Dependencies {
    override val dataSourceFactory = DataSourceFactory(config)
    override val metadata = OnedriveMetadataRepository(dataSourceFactory)
    override val ticker = Ticker()
    override val onedriveClients = OnedriveClientsImpl()
    override val oneDrive = OnedriveWrapper(onedriveClients, config)
    override val userRepository = UserRepository(dataSourceFactory)
}