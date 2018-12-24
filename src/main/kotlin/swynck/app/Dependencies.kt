package swynck.app

import swynck.config.Config
import swynck.daemon.DaemonRunner
import swynck.db.DataSourceFactory
import swynck.db.OnedriveMetadataRepository
import swynck.db.UserRepository
import swynck.service.Onedrive
import java.time.Clock

class Dependencies(val config: Config = Config()) {
    private val clock = Clock.systemUTC()
    val dataSourceFactory = DataSourceFactory(config)
    val daemonRunner = DaemonRunner()
    val metadata = OnedriveMetadataRepository(dataSourceFactory)
    val oneDrive = Onedrive(config)
    val userRepository = UserRepository(dataSourceFactory)
}