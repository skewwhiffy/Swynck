package swynck.app

import swynck.config.Config
import swynck.daemon.DaemonRunner
import swynck.db.DataSourceFactory
import swynck.db.UserRepository
import swynck.service.Onedrive

class Dependencies(val config: Config = Config()) {
    val dataSourceFactory = DataSourceFactory(config)
    val userRepository = UserRepository(dataSourceFactory)
    val oneDrive = Onedrive(config)
    val daemonRunner = DaemonRunner()
}