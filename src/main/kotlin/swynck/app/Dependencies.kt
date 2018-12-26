package swynck.app

import swynck.config.Config
import swynck.daemon.DaemonRunner
import swynck.db.DataSourceFactory
import swynck.db.OnedriveMetadataRepository
import swynck.db.UserRepository
import swynck.service.Onedrive

open class Dependencies(
    val config: Config = Config(),
    val daemonRunner: DaemonRunner = DaemonRunner()
) {
    val dataSourceFactory = DataSourceFactory(config)
    val metadata = OnedriveMetadataRepository(dataSourceFactory)
    val oneDrive = Onedrive(config)
    val userRepository = UserRepository(dataSourceFactory)
}