package swynck.test.utils

import swynck.config.Config
import java.net.ServerSocket
import java.util.*

class TestConfig : Config {
    private val dbName = "${UUID.randomUUID()}"
    var port = ServerSocket(0).use { it.localPort }

    override fun port() = port

    override fun db() = "jdbc:h2:mem:$dbName;DB_CLOSE_DELAY=-1"
}