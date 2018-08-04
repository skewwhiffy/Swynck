package swynck.app

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.cancelAndJoin
import kotlinx.coroutines.experimental.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.http4k.client.OkHttp
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.net.ServerSocket

class RunTests {
    companion object {
        private lateinit var server: Deferred<*>
        private lateinit var client: OkHttp
        private var webport: Int = 0

        @JvmStatic
        @BeforeClass
        fun initClass() {
            webport = ServerSocket(0).use { it.localPort }
            server = async {
                try {
                    Run(webport)
                } catch (e: Exception) {
                    println(e)
                    throw e
                }
            }
            client = OkHttp()
            var attempts = 10
            while(true) {
                val response = client(Request(GET, "http://localhost:$webport/ping"))
                if (response.status.successful) return
                Thread.sleep(100)
                attempts--
                if (attempts < 0) throw IllegalStateException("Server did not come up")
            }
        }

        @JvmStatic
        @AfterClass
        fun teardownClass() {
            runBlocking { server.cancelAndJoin() }
        }
    }

    @Test
    fun `dummy test`() {
        val response = client(Request(GET, "http://localhost:$webport/ping"))

        assertThat(response.bodyString()).isEqualTo("pong")
    }
}