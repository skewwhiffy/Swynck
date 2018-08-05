package swynck.test.utils

import swynck.config.Config
import java.net.ServerSocket

class TestConfig : Config {
    private val port: Int = ServerSocket(0).use { it.localPort }

    override fun port() = port

    override fun db() = "jdbc:h2:mem:"
}