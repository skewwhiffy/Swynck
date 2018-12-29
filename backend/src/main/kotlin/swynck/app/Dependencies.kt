package swynck.app

import swynck.config.Config
import swynck.daemon.DaemonRunner
import swynck.db.DataSourceFactory
import swynck.db.OnedriveMetadataRepository
import swynck.db.UserRepository
import swynck.service.Onedrive

interface Dependencies {
    companion object {
        operator fun invoke() = DependenciesImpl()
    }
    val config: Config
    val dataSourceFactory: DataSourceFactory
    val daemonRunner: DaemonRunner
    val metadata: OnedriveMetadataRepository
    val oneDrive: Onedrive
    val userRepository: UserRepository
}

class DependenciesImpl(
    override val config: Config = Config(),
    override val daemonRunner: DaemonRunner = DaemonRunner()
): Dependencies {
    override val dataSourceFactory = DataSourceFactory(config)
    override val metadata = OnedriveMetadataRepository(dataSourceFactory)
    override val oneDrive = Onedrive(config)
    override val userRepository = UserRepository(dataSourceFactory)
}