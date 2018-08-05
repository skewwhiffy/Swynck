package swynck.test.utils

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.cancelAndJoin
import kotlinx.coroutines.experimental.runBlocking
import org.http4k.client.OkHttp
import org.http4k.core.Method.GET
import org.http4k.core.Request
import swynck.app.Run
import swynck.config.Config

class StartServerForTesting : AutoCloseable {
    val config: Config = TestConfig()
    val dataSourceFactory = SingletonDataSourceFactory(config)
    private val server = async {
        try {
            Run(config, dataSourceFactory)
        } catch (e: Exception) {
            println(e)
            throw e
        }
    }
    val client = OkHttp()

    init {
        var attempts = 10
        while(true) {
            val response = client(Request(GET, "http://localhost:${config.port()}/ping"))
            if (response.status.successful) break
            Thread.sleep(100)
            attempts--
            if (attempts < 0) throw IllegalStateException("Server did not come up")
        }
    }

    override fun close() {
        runBlocking { server.cancelAndJoin() }
    }
}